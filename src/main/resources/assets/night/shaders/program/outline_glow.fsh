#version 330

// Pass 2 of ShadersModule's Glow: vertical half of the separable blur (over pass 1's already
// horizontally-blurred, PREMULTIPLIED output), then composite -- same blur-and-composite-in-one
// shape star_glow.fsh uses, so Glow costs 3 passes total (h-blur, this, blit), not 4.
//
// The input is already premultiplied by pass 1, so this half is a plain linear blur; only the
// composite un-premultiplies. `blurred.a` IS the coverage: the weighted fraction of the 2D
// neighbourhood that was silhouette -- ~1 deep inside a body, ~0.5 right on the edge, ->0 far
// outside. One number, read in OPPOSITE directions on the two sides of the edge:
//
//   outside  fades OUT as coverage drops        (halo bleeding past the silhouette)
//   inside   fades IN  as coverage drops        (haze over the entity, brightest at the rim)
//
// The inside branch is deliberately the stronger of the two at matching coverage, and floors at
// GLOW_BODY rather than going clear deep inside. The entity's own texture is still on screen
// underneath either way -- nothing here discards it the way outline.fsh's Fill branch replaces it.
//
// 2026-08-21 FIX (reported: "Mode Glow khi bật thì Shaders 9 mode không được áp dụng"). This pass
// only ever used the raw captured silhouette color (original.rgb/blurred.rgb) -- ShadersModule's
// own "Shader" setting (Gradient/Rainbow/Garmadon/.../Neekeri, EspParams.w) was never read here at
// all, unlike outline.fsh's Fill branch which tints via esp_color(effect). Same esp_color function
// library duplicated in verbatim below (outline.fsh already duplicates it from esp.fsh -- same
// established pattern in this codebase, not new duplication for its own sake: no confirmed
// #moj_import support for a shared night:-namespace .glsl outside vanilla's own minecraft:
// namespace, so copying stays the safe option) -- EspSettings uniform block added so
// EspShader#writeOutlineSettings' existing "for every pass with an EspSettings key" loop picks
// this pass up automatically, no Java changes needed.

uniform sampler2D InSampler;
uniform sampler2D OriginalSampler;
uniform sampler2D ImageSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform OutlineGlowConfigV {
    vec4 Params;
    vec4 GlowColor;
};
#define Intensity Params.x
#define Radius Params.y
#define IsGlowRainbow (Params.z > 0.5)
#define Direction vec2(0.0, 1.0)

layout(std140) uniform EspSettings {
    vec4 Rgb0;
    vec4 Rgb1;
    vec4 Rgb2;
    vec4 Rgb3;
    vec4 EspParams;
    vec4 Extra;
};
#define Time EspParams.x
#define Step EspParams.y
#define Dist EspParams.z
#define Resolution OutSize
// ShaderOpacity slider (0..1), written into Extra.x by EspShader#writeOutlineSettings the same
// way outline.fsh reads it -- was missing here entirely, which is why the 2026-08-26 fill fix
// below read as "fill always full opacity, ShaderOpacity slider does nothing" for every mode.
#define ShaderOpacity Extra.x

// Must match outline_glow_blur.fsh's MAX_WIDTH and EspShader#writeOutlineSettings' radius cap.
#define MAX_WIDTH 24

// Opacity floor deep inside a body, and the outside halo's damping relative to the inside haze.
#define GLOW_BODY 0.25
#define GLOW_OUTER 0.7

in vec2 texCoord;
out vec4 fragColor;

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
    float distance = sqrt((gl_FragCoord.x * Dist) * (gl_FragCoord.x * Dist) + (gl_FragCoord.y * Dist) * (gl_FragCoord.y * Dist)) + Time;
    float distance2 = sqrt(((gl_FragCoord.x * Dist) - 1920.0) * ((gl_FragCoord.x * Dist) - 1920.0) + (gl_FragCoord.y * Dist) * (gl_FragCoord.y * Dist)) + Time;
    float distance3 = sqrt(((gl_FragCoord.x * Dist) - 1080.0) * ((gl_FragCoord.x * Dist) - 1080.0) + ((gl_FragCoord.y * Dist) - 1080.0) * ((gl_FragCoord.y * Dist) - 1080.0)) + Time;

    distance = sin(distance / Step + sin((gl_FragCoord.y * Dist) / Step)) * 0.5 + 0.5;
    distance2 = sin(distance2 / Step + sin((gl_FragCoord.x * Dist) / Step)) * 0.5 + 0.5;
    distance3 = sin(distance3 / Step + sin((gl_FragCoord.y * Dist) / Step + (gl_FragCoord.x * Dist) / Step)) * 0.5 + 0.5;

    float ripple = sin(distance * 10.0 + Step * 10000.0) * 0.05;
    float swirl = cos(((gl_FragCoord.x * Dist) - 960.0) * 0.05 + ((gl_FragCoord.y * Dist) - 540.0) * 0.05 + Step) * 0.05;

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
    float sat = Extra.w > 0.0 ? Extra.w : 1.0;
    float bri = Dist > 0.0 ? Dist : 1.0;
    float scale = Step > 0.0 ? Step : 1.0;
    float wave = (gl_FragCoord.x + gl_FragCoord.y) * 0.003 * scale;
    return esp_hsv2rgb(vec3(fract(wave + Time), sat, bri));
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

vec4 esp_garmadon() {
    vec2 p = (gl_FragCoord.xy * 3.0 - Resolution.xy) / min(Resolution.x, Resolution.y) * Dist;
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

    vec2 uv = gl_FragCoord.xy / Resolution.xy;
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
    vec2 p = (gl_FragCoord.xy * 2.0 - Resolution.xy) / min(Resolution.x, Resolution.y) * Dist;
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
    vec2 p = (gl_FragCoord.xy / Resolution.xy) * Dist;
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
    vec2 uv = (gl_FragCoord.xy * Dist) / Resolution.xy;

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
    vec2 coord = gl_FragCoord.xy * Dist;
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
    vec2 st = (gl_FragCoord.xy / Resolution.y) * Dist;
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
    vec2 coord = gl_FragCoord.xy * Dist;
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

vec4 esp_image() {
    return texture(ImageSampler, vec2(texCoord.x, 1.0 - texCoord.y));
}

// ----------------------------------------------------------------

// .rgb is the animated color, .a an extra alpha multiplier (only garmadon/neekeri use it, same as
// esp.fsh's `extra`). Effect 0 returns the identity so callers can multiply unconditionally.
vec4 esp_color(int effect) {
    if (effect == 1) return vec4(esp_gradient(), 1.0);
    if (effect == 2) return vec4(esp_rainbow(), 1.0);
    if (effect == 3) return esp_garmadon();
    if (effect == 4) return vec4(esp_nebula(), 1.0);
    if (effect == 5) return vec4(esp_blue(), 1.0);
    if (effect == 6) return vec4(esp_purple(), 1.0);
    if (effect == 7) return vec4(esp_glowing(), 1.0);
    if (effect == 8) return vec4(esp_pink(), 1.0);
    // esp_neekeri()'s alpha channel is bogus (colour_1.a=11.0, an unrelated intensity constant
    // from the original port, never a real alpha) -- using it as esp.a blows the multiplied
    // fragColor.a way past 1.0, which the driver just clamps to fully opaque: the "Neekeri
    // swallows everything" bug. Every other mode here returns alpha=1; force the same for
    // Neekeri, only its RGB (legitimate) is real.
    if (effect == 9) return vec4(esp_neekeri().rgb, 1.0);
    if (effect == 10) return esp_image();
    return vec4(0.0, 0.0, 0.0, 1.0);
}

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec4 sum = vec4(0.0);
    float weight = 0.0;

    for (float i = -MAX_WIDTH; i <= MAX_WIDTH; i++) {
        float d = abs(i);
        if (d > Radius) continue;

        float w = 1.0 - d / Radius;
        sum += texture(InSampler, texCoord + Direction * i * oneTexel) * w;
        weight += w;
    }

    vec4 blurred = weight > 0.0 ? sum / weight : vec4(0.0);
    float coverage = blurred.a;

    // Nothing within a full blur radius -- leave the pixel untouched. Safe to discard rather than
    // write a transparent pixel: the outline_outline variant's own inside-discard already relies on
    // the "swap" target being cleared per frame.
    if (coverage <= 0.0) discard;

    // Brightness alone saturates well before the slider's top end, so Intensity drives the blur
    // Radius too (EspShader#writeOutlineSettings) -- same lesson as StarGlow's own ramp.
    float gain = 0.6 + Intensity * 1.4;
    vec4 original = texture(OriginalSampler, texCoord);

    // Same effect-tint rule as outline.fsh's Fill branch: effect 0 (None) leaves the captured
    // color untouched; Glowing (7) already consumes Color itself (esp_glowing returns
    // Rgb0.rgb / wave, Rgb0 IS Color) so tinting it again would square the color; every other
    // mode multiplies the animated pattern by the captured Color the same way Fill does.
    int effect = int(EspParams.w + 0.5);
    vec4 esp = effect != 0 ? esp_color(effect) : vec4(1.0);

    float rainbowSat = Extra.w > 0.0 ? Extra.w : 1.0;
    float rainbowBri = Dist > 0.0 ? Dist : 1.0;
    float rainbowScale = Step > 0.0 ? Step : 1.0;
    vec3 spatialRainbow = esp_hsv2rgb(vec3(fract((gl_FragCoord.x + gl_FragCoord.y) * 0.003 * rainbowScale + Time), rainbowSat, rainbowBri));

    vec3 blurredRgb = blurred.rgb / coverage;
    vec3 glowRainbowRgb = spatialRainbow;
    vec3 glowRgb = IsGlowRainbow ? glowRainbowRgb : (GlowColor.a > 0.0 ? GlowColor.rgb : blurredRgb);
    float glowAlpha = GlowColor.a > 0.0 ? GlowColor.a : 1.0;

    if (original.a != 0.0) {
        vec4 baseFill;
        float colorAlpha = Rgb0.a;
        vec3 originalBase = (Extra.z > 0.5) ? spatialRainbow : Rgb0.rgb;

        if (effect == 0) {
            baseFill = vec4(originalBase, ShaderOpacity);
        } else if (effect == 10) {
            if (esp.a == 0.0) {
                baseFill = vec4(originalBase, ShaderOpacity);
            } else {
                vec3 imageRgb = mix(esp.rgb, originalBase, colorAlpha);
                baseFill = vec4(imageRgb, clamp(ShaderOpacity * esp.a, 0.0, 1.0));
            }
        } else {
            vec3 rgb = (effect == 7) ? esp.rgb : esp.rgb * originalBase;
            baseFill = vec4(rgb, clamp(ShaderOpacity * esp.a, 0.0, 1.0));
        }

        // Inner glow: radiates from the outer rim (coverage ~0.5) inward towards the entity center.
        // Floods deeper into the fill as Intensity increases up to 100%.
        float rim = clamp((1.0 - coverage) * 2.0, 0.0, 1.0);
        float innerGlowFactor = clamp((rim * 0.75 + Intensity * 0.4) * gain * glowAlpha, 0.0, 1.0);
        vec3 finalRgb = mix(baseFill.rgb, glowRgb, innerGlowFactor);
        float finalAlpha = clamp(baseFill.a + innerGlowFactor * (1.0 - baseFill.a) * GLOW_OUTER, 0.0, 1.0);
        fragColor = vec4(finalRgb, finalAlpha);
        return;
    }

    fragColor = vec4(glowRgb, clamp(pow(coverage, 0.7) * gain * GLOW_OUTER * glowAlpha, 0.0, 1.0));
}
