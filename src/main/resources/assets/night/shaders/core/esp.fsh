#version 330

// Ported from Sydney-Legacy's assets/sydney/shaders_new/*.frag (9 separate #version 150 programs,
// each bound by hand with raw glUseProgram + glUniform1f). 26.1.2 has no runtime uniform setter and
// no immediate-mode program binding -- uniforms only reach a shader through a std140 UBO declared on
// the RenderPipeline. Nine pipelines would mean nine UBOs and nine RenderPipeline objects for what is
// really one draw call with a different colour function, so the nine programs are folded into ONE
// fragment shader switched by the Effect index in the UBO below. The per-mode maths is otherwise
// verbatim from Sydney; only the uniform NAMES changed (time->Time, resolution->Resolution,
// alpha->Alpha, dist->Dist, rgb*->Rgb*, step->Step -- `step` had to be renamed regardless, it
// shadowed the GLSL builtin).
//
// Declares NO vanilla uniform blocks on purpose: this one .fsh is paired with BOTH vanilla's
// core/position_color vertex shader (quads pipeline, which only declares Projection +
// DynamicTransforms) and core/rendertype_lines (lines pipeline, which additionally declares Fog +
// Globals). Referencing a block the bound pipeline doesn't declare would fail to link, and unused
// declared blocks are skipped harmlessly by GlProgram.setupUniforms, so screen size is carried in
// our own UBO rather than read from Globals.ScreenSize.

in vec4 vertexColor;

out vec4 fragColor;

layout(std140) uniform EspSettings {
    vec4 Rgb0;
    vec4 Rgb1;
    vec4 Rgb2;
    vec4 Rgb3;
    vec2 Resolution;
    float Time;
    float Step;
    float Alpha;
    float Dist;
    int Effect;
    vec2 Offset;
};

// ---------------------------------------------------------------- shared noise (garmadon/nebula/pink)

// 2026-09-09 FIX (reported: Garmadon/Nebula render with hard square/grid seams). The old
// esp_random + esp_noise_from pair was VALUE noise (bilinear-interpolated raw hash SCALARS, cubic
// C1-only smoothstep) -- a well-known-blocky noise family with visible cell boundaries by
// construction, independent of octave count. AtmosphereModule's own sky.fsh (ModifySky>CustomSky
// Nebula/Smoke) already solved this exact class of bug by switching to GRADIENT noise (Perlin-
// style: interpolate HASH VECTORS dotted against the corner-to-point offset, not raw scalars) with
// quintic (C2-continuous, not just C1) smoothing -- ported verbatim here, same technique, same
// esp_noise/esp_noise_nebula names so esp_fbm_garmadon/esp_fbm_nebula below need no changes.
vec2 esp_hash_grad(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

vec2 esp_hash_grad_nebula(vec2 p) {
    p = vec2(dot(p, vec2(1399.9898, 78.233)), dot(p, vec2(269.5, 1183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float esp_noise(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    float n00 = dot(esp_hash_grad(i + vec2(0.0, 0.0)), f - vec2(0.0, 0.0));
    float n10 = dot(esp_hash_grad(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0));
    float n01 = dot(esp_hash_grad(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0));
    float n11 = dot(esp_hash_grad(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0));
    return 0.5 + 0.5 * mix(mix(n00, n10, u.x), mix(n01, n11, u.x), u.y);
}

float esp_noise_nebula(vec2 pos) {
    vec2 i = floor(pos);
    vec2 f = fract(pos);
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    float n00 = dot(esp_hash_grad_nebula(i + vec2(0.0, 0.0)), f - vec2(0.0, 0.0));
    float n10 = dot(esp_hash_grad_nebula(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0));
    float n01 = dot(esp_hash_grad_nebula(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0));
    float n11 = dot(esp_hash_grad_nebula(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0));
    return 0.5 + 0.5 * mix(mix(n00, n10, u.x), mix(n01, n11, u.x), u.y);
}

// ---------------------------------------------------------------- 1: gradient

vec3 esp_gradient() {
    vec2 coord = gl_FragCoord.xy + Offset;
    float distance = sqrt((coord.x * Dist) * (coord.x * Dist) + (coord.y * Dist) * (coord.y * Dist)) + Time;
    float distance2 = sqrt(((coord.x * Dist) - 1920.0) * ((coord.x * Dist) - 1920.0) + (coord.y * Dist) * (coord.y * Dist)) + Time;
    float distance3 = sqrt(((coord.x * Dist) - 1080.0) * ((coord.x * Dist) - 1080.0) + ((coord.y * Dist) - 1080.0) * ((coord.y * Dist) - 1080.0)) + Time;

    distance = sin(distance / Step + sin((coord.y * Dist) / Step)) * 0.5 + 0.5;
    distance2 = sin(distance2 / Step + sin((coord.x * Dist) / Step)) * 0.5 + 0.5;
    distance3 = sin(distance3 / Step + sin((coord.y * Dist) / Step + (coord.x * Dist) / Step)) * 0.5 + 0.5;

    float ripple = sin(distance * 10.0 + Step * 10000.0) * 0.05;
    float swirl = cos(((coord.x * Dist) - 960.0) * 0.05 + ((coord.y * Dist) - 540.0) * 0.05 + Step) * 0.05;

    distance += ripple + swirl;
    distance2 += ripple - swirl;
    distance3 += ripple + swirl;

    float inv = 1.0 - distance;
    return Rgb0.rgb * distance + Rgb1.rgb * inv + Rgb2.rgb * distance2 + Rgb3.rgb * distance3;
}

// ---------------------------------------------------------------- 2: rainbow

vec3 esp_hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 esp_rainbow() {
    float wave = (gl_FragCoord.x + gl_FragCoord.y) * 0.003;
    return esp_hsv2rgb(vec3(fract(wave + Time * 0.05), 1.0, 1.0));
}

// ---------------------------------------------------------------- 3: garmadon

float esp_fbm_garmadon(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 6; i++) {
        float dir = mod(float(i), 2.0) > 0.5 ? 1.0 : -1.0;
        v += a * esp_noise(pos - 0.05 * dir * Time);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

// Returns rgb in .rgb and an extra alpha multiplier in .a. Sydney's garmadon.frag declared a local
// `float alpha` that shadowed the uniform of the same name and then wrote `alpha * vertexColor.a *
// alpha` -- squaring the local and dropping the configured opacity entirely, so the Opacity slider
// did nothing in that one mode. Kept the local falloff, restored the uniform as the third factor.
vec4 esp_garmadon() {
    vec2 coord = gl_FragCoord.xy + Offset;
    vec2 p = (coord * 3.0 - Resolution.xy) / min(Resolution.x, Resolution.y) * Dist;
    p -= vec2(12.0, 0.0);

    float time2 = 1.0;
    vec2 q = vec2(0.0);
    q.x = esp_fbm_garmadon(p + 0.00 * time2);
    q.y = esp_fbm_garmadon(p + vec2(1.0));
    vec2 r = vec2(0.0);
    r.x = esp_fbm_garmadon(p + 1.0 * q + vec2(1.7, 1.2) + 0.15 * time2);
    r.y = esp_fbm_garmadon(p + 1.0 * q + vec2(8.3, 2.8) + 0.126 * time2);
    float f = esp_fbm_garmadon(p + r);

    vec3 color = mix(vec3(1.0, 1.0, 2.0), vec3(1.0, 1.0, 1.0), clamp((f * f) * 5.5, 1.2, 15.5));
    color = mix(color, vec3(1.0, 1.0, 1.0), clamp(length(q), 2.0, 2.0));
    color = mix(color, vec3(0.3, 0.2, 1.0), clamp(length(r.x), 0.0, 5.0));
    color = (f * f * f * 1.0 + 0.5 * 1.7 * 0.0 + 0.9 * f) * color;

    vec2 uv = coord / Resolution.xy;
    float falloff = clamp(50.0 - max(pow(100.0 * distance(uv.x, -1.0), 0.0), pow(2.0 * distance(uv.y, 0.5), 5.0)), 0.0, 1.0);
    return vec4(color, falloff);
}

// ---------------------------------------------------------------- 4: nebula

float esp_fbm_nebula(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 6; i++) {
        v += a * esp_noise_nebula(pos);
        pos = rot * pos * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

vec3 esp_nebula() {
    vec2 coord = gl_FragCoord.xy + Offset;
    vec2 p = (coord * 2.0 - Resolution.xy) / min(Resolution.x, Resolution.y) * Dist;
    float time2 = 0.6 * Time / 2.0;

    vec2 q = vec2(2.0);
    q.x = esp_fbm_nebula(p + 1.30 * time2);
    q.y = esp_fbm_nebula(p + vec2(1.0));
    vec2 r = vec2(0.0);
    r.x = esp_fbm_nebula(p + 1.0 * q + vec2(1.2, 3.2) + 0.135 * time2);
    r.y = esp_fbm_nebula(p + 1.0 * q + vec2(8.8, 2.8) + 0.126 * time2);
    float f = esp_fbm_nebula(p + r);

    vec3 color = mix(vec3(0.0, 0.0, 0.0), vec3(1.0, 0.0, 0.7), clamp((f * f) * 5.1, 0.0, 5.0));
    color = mix(color, vec3(0.0, 0.0, 1.0), clamp(length(q), 0.0, 1.0));
    color = mix(color, vec3(0.3, 1.0, 1.0), clamp(length(r.x), 0.0, 1.0));
    return (f * f * f + 0.6 * f * f + 0.9 * f) * color;
}

// ---------------------------------------------------------------- 5: blue (classic perlin)

vec3 esp_mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 esp_mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 esp_permute(vec4 x) { return esp_mod289(((x * 34.0) + 10.0) * x); }
vec4 esp_taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }
vec3 esp_fade(vec3 t) { return t * t * t * (t * (t * 6.0 - 15.0) + 10.0); }

float esp_cnoise(vec3 P) {
    vec3 Pi0 = floor(P);
    vec3 Pi1 = Pi0 + vec3(1.0);
    Pi0 = esp_mod289(Pi0);
    Pi1 = esp_mod289(Pi1);
    vec3 Pf0 = fract(P);
    vec3 Pf1 = Pf0 - vec3(1.0);
    vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
    vec4 iy = vec4(Pi0.yy, Pi1.yy);
    vec4 iz0 = Pi0.zzzz;
    vec4 iz1 = Pi1.zzzz;

    vec4 ixy = esp_permute(esp_permute(ix) + iy);
    vec4 ixy0 = esp_permute(ixy + iz0);
    vec4 ixy1 = esp_permute(ixy + iz1);

    vec4 gx0 = ixy0 * (1.0 / 7.0);
    vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
    gx0 = fract(gx0);
    vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
    vec4 sz0 = step(gz0, vec4(0.0));
    gx0 -= sz0 * (step(0.0, gx0) - 0.5);
    gy0 -= sz0 * (step(0.0, gy0) - 0.5);

    vec4 gx1 = ixy1 * (1.0 / 7.0);
    vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
    gx1 = fract(gx1);
    vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
    vec4 sz1 = step(gz1, vec4(0.0));
    gx1 -= sz1 * (step(0.0, gx1) - 0.5);
    gy1 -= sz1 * (step(0.0, gy1) - 0.5);

    vec3 g000 = vec3(gx0.x, gy0.x, gz0.x);
    vec3 g100 = vec3(gx0.y, gy0.y, gz0.y);
    vec3 g010 = vec3(gx0.z, gy0.z, gz0.z);
    vec3 g110 = vec3(gx0.w, gy0.w, gz0.w);
    vec3 g001 = vec3(gx1.x, gy1.x, gz1.x);
    vec3 g101 = vec3(gx1.y, gy1.y, gz1.y);
    vec3 g011 = vec3(gx1.z, gy1.z, gz1.z);
    vec3 g111 = vec3(gx1.w, gy1.w, gz1.w);

    vec4 norm0 = esp_taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
    g000 *= norm0.x;
    g010 *= norm0.y;
    g100 *= norm0.z;
    g110 *= norm0.w;
    vec4 norm1 = esp_taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
    g001 *= norm1.x;
    g011 *= norm1.y;
    g101 *= norm1.z;
    g111 *= norm1.w;

    float n000 = dot(g000, Pf0);
    float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
    float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
    float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
    float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
    float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
    float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
    float n111 = dot(g111, Pf1);

    vec3 fade_xyz = esp_fade(Pf0);
    vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
    vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
    return 2.2 * mix(n_yz.x, n_yz.y, fade_xyz.x);
}

float esp_color_noise(vec2 xy) {
    return esp_cnoise(vec3(1.5 * xy, 0.3 * Time));
}

vec3 esp_blue() {
    vec2 coord = gl_FragCoord.xy + Offset;
    vec2 p = (coord / Resolution.xy) * Dist;
    vec2 stepping = vec2(1.3, 1.7);
    float n = esp_color_noise(p);
    n += 0.5 * esp_color_noise(p * 2.0 - stepping);
    n += 0.25 * esp_color_noise(p * 4.0 - 2.0 * stepping);
    n += 0.125 * esp_color_noise(p * 8.0 - 3.0 * stepping);
    n += 0.0625 * esp_color_noise(p * 16.0 - 4.0 * stepping);
    n += 0.03125 * esp_color_noise(p * 32.0 - 5.0 * stepping);
    return 0.5 + 0.5 * vec3(n, n, n + 1.0);
}

// ---------------------------------------------------------------- 6: purple

vec3 esp_purple() {
    float time2 = Time * 0.12;
    vec2 coord = gl_FragCoord.xy + Offset;
    vec2 uv = (coord * Dist) / Resolution.xy;

    vec2 p = mod(uv * 6.28318530718, 6.28318530718) - 254.0;
    vec2 i = vec2(p);
    float c = 1.2;
    float inten = 0.0064;

    for (int n = 0; n < 5; n++) {
        float t = time2 * (1.0 - (7.2 / float(n + 1)));
        i = p + vec2(cos(t - i.x) + sin(t - i.y), sin(t - i.y) + cos(t + i.x));
        c += 1.0 / length(vec2(p.x / (sin(i.x + t) / inten), p.y / (cos(i.y + t) / inten)));
    }

    c /= 5.0;
    c = 1.23 - pow(c, 1.22);
    return vec3(0.1 + pow(abs(c), 19.2), 0.1 + pow(abs(c), 50.2), 0.12 + pow(abs(c), 5.0));
}

// ---------------------------------------------------------------- 7: glowing

vec3 esp_glowing() {
    vec2 coord = (gl_FragCoord.xy + Offset) * Dist;
    vec2 uv = (2.0 * coord - Resolution.xy) / min(Resolution.x, Resolution.y);

    for (float i = 1.0; i < 10.0; i++) {
        uv.x += 0.6 / i * cos(i * 2.5 * uv.y + Time);
        uv.y += 0.6 / i * cos(i * 1.5 * uv.x + Time);
    }

    float wave = abs(sin(Time - uv.y - uv.x));
    return vec3(Rgb0.r / wave, Rgb0.g / wave, Rgb0.b / wave);
}

// ---------------------------------------------------------------- 8: pink

float esp_rotate_doit(vec2 uv) {
    float t = Time * 0.66;
    vec2 n = vec2(0);
    vec2 q = vec2(0);
    vec2 p = uv;
    float d = 0.0;
    float S = 18.0;
    float a = -0.004;
    mat2 m = mat2(0.415, tan(0.12 * 5.0), -sin(5.0), cos(5.0));

    for (float j = 0.0; j < 5.0; j++) {
        p *= m;
        n *= m * 0.635;
        q = p * S + t * 4.0 + sin(t * 1.0 - d * 8.0) * 0.0018 + 3.0 * j - 0.95 * n;
        a += dot(cos(q) / S, vec2(0.256));
        n -= sin(q);
        S *= 1.44;
    }
    return a;
}

float esp_fbm_pink(vec2 st) {
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < 5; ++i) {
        v += a * esp_noise(st);
        st = rot * st * 2.0 + shift;
        a *= 0.515;
    }
    return v;
}

vec3 esp_pink() {
    vec2 coord = gl_FragCoord.xy + Offset;
    vec2 st = (coord / Resolution.y) * Dist;
    float a = abs(esp_rotate_doit(st * 0.5) * 12.0);
    st.x *= 0.3;

    vec2 q = vec2(0.0);
    q.x = esp_fbm_pink(st);
    q.y = esp_fbm_pink(st + vec2(1.0));

    vec2 r = vec2(0.0);
    r.x = esp_fbm_pink(st + 1.0 * q + vec2(1.7, 9.2) + 0.055 * Time);
    r.y = esp_fbm_pink(st + 1.0 * q + vec2(8.3, 2.8) + 0.0126 * Time);

    float f = esp_fbm_pink(st + r);

    vec3 color = mix(vec3(0.666667, 0.101961, 0.19608), vec3(0.666667, 0.666667, 0.48039), clamp((f * f) * 4.0, 0.0, 1.0));
    color = mix(color, vec3(0.164706, 0.0, 0.0), clamp(length(q), 0.0, 1.0));
    color = mix(color, vec3(1.0, 0.666667, 1.0), clamp(length(r.x), 0.0, 1.0));
    color += a * a * a;

    return (f * f * f + 0.6 * f * f + 0.5 * f) * color * 1.9;
}

// ---------------------------------------------------------------- 9: neekeri

vec4 esp_neekeri() {
    const vec4 colour_2 = vec4(1.0, 200.0 / 215.0, 1.0, 0.6);
    const vec4 colour_1 = vec4(0.15, 0.12, 0.2, 11.0);
    const vec4 colour_3 = vec4(0.0, 0.0, 0.0, 1.0);
    const float spin_amount = 0.7;
    const float contrast = 1.5;

    float pixel_size = length(Resolution.xy) / 711100.0;
    vec2 coord = (gl_FragCoord.xy + Offset) * Dist;
    vec2 uv = (floor(coord * (1.0 / pixel_size)) * pixel_size - 0.5 * Resolution.xy) / length(Resolution.xy);
    float uv_len = length(uv);

    float speed = (Time * 0.5 * 0.1) + 302.2;
    float new_pixel_angle = (atan(uv.y, uv.x)) + speed - 0.5 * 20.0 * (1.0 * spin_amount * uv_len + (1.0 - 1.0 * spin_amount));
    vec2 mid = (Resolution.xy / length(Resolution.xy)) / 2.0;
    uv = (vec2((uv_len * cos(new_pixel_angle) + mid.x), (uv_len * sin(new_pixel_angle) + mid.y)) - mid);

    uv *= 30.0;
    speed = Time;
    vec2 uv2 = vec2(uv.x + uv.y);

    for (int i = 0; i < 5; i++) {
        uv2 += uv + cos(length(uv));
        uv += 0.5 * vec2(cos(5.1123314 + 0.353 * uv2.y + speed * 0.131121), sin(uv2.x - 0.113 * speed));
        uv -= 1.0 * cos(uv.x + uv.y) - 1.0 * sin(uv.x * 0.711 - uv.y);
    }

    float contrast_mod = (0.25 * contrast + 0.5 * spin_amount + 1.2);
    float paint_res = min(2.0, max(0.0, length(uv) * (0.035) * contrast_mod));
    float c1p = max(0.0, 1.0 - contrast_mod * abs(1.0 - paint_res));
    float c2p = max(0.0, 1.0 - contrast_mod * abs(paint_res));
    float c3p = 1.0 - min(1.0, c1p + c2p);

    vec4 ret_col = (0.3 / contrast) * colour_1
        + (1.0 - 0.3 / contrast) * (colour_1 * c1p + colour_2 * c2p + vec4(c3p * colour_3.rgb, c3p * colour_1.a))
        + 0.3 * max(c1p * 5.0 - 4.0, 0.0)
        + 0.4 * max(c2p * 5.0 - 4.0, 0.0);

    return ret_col;
}

// ----------------------------------------------------------------

void main() {
    if (vertexColor.a == 0.0) {
        discard;
    }

    vec3 color;
    float extra = 1.0;

    if (Effect == 1) {
        color = esp_gradient();
    } else if (Effect == 2) {
        color = esp_rainbow();
    } else if (Effect == 3) {
        vec4 g = esp_garmadon();
        color = g.rgb;
        extra = g.a;
    } else if (Effect == 4) {
        color = esp_nebula();
    } else if (Effect == 5) {
        color = esp_blue();
    } else if (Effect == 6) {
        color = esp_purple();
    } else if (Effect == 7) {
        color = esp_glowing();
    } else if (Effect == 8) {
        color = esp_pink();
    } else {
        // esp_neekeri()'s colour_1.a is 11.0 (an unrelated intensity constant from the original
        // port, never meant as a real alpha) -- using it as `extra` blows fragColor.a way past
        // 1.0, which the driver just clamps to fully opaque. That's the "Neekeri swallows
        // everything" bug: every other mode here returns/leaves extra at 1.0, only Neekeri's
        // silhouette rendered as a solid opaque blob instead of a normal translucent shape. RGB
        // is unaffected (colour_1.rgb is legitimate), only the alpha channel was ever bogus.
        color = esp_neekeri().rgb;
    }

    fragColor = vec4(color, vertexColor.a * Alpha * extra);
}
