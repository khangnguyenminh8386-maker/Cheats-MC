#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MenuBgConfig {
    vec4 Params; // x: Time, y: Alpha, z: TransitionProgress (0.0 -> 1.0), w: unused
};

in vec2 texCoord;
out vec4 fragColor;

vec3 tanh_approx(vec3 x) {
    vec3 ex = exp(clamp(2.0 * x, -20.0, 20.0));
    return (ex - 1.0) / (ex + 1.0);
}

// ----------------------------------------------------
// SHADER 1: Volumetric Cloudy Night Moon
// ----------------------------------------------------

#define CLOUDS_SMOOTHNESS 1.5
#define LIGHT_INTENSITY   20.0
#define ABSORPTION        5.5
#define VOLUME_STEPS      30.0

float hash33_val(vec3 p3) {
    p3 = fract(p3 * 0.1031 + vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

float noise3D(vec3 p) {
    p += vec3(127.1, 311.7, 74.7);
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);

    float n000 = hash33_val(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash33_val(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash33_val(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash33_val(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash33_val(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash33_val(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash33_val(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash33_val(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);

    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);

    return mix(nxy0, nxy1, u.z);
}

float fbm3D(vec3 x) {
    float rz = 0.0;
    float a = 0.35;
    for (int i = 0; i < 4; i++) {
        rz += noise3D(x) * a;
        a *= 0.35;
        x *= 3.8;
    }
    return rz - 0.25;
}

vec2 boxIntersection(vec3 ro, vec3 rd) {
    vec3 rad = vec3(6.0, 6.0, 2.0);
    vec3 safeRd = vec3(
        abs(rd.x) < 1e-5 ? (rd.x >= 0.0 ? 1e-5 : -1e-5) : rd.x,
        abs(rd.y) < 1e-5 ? (rd.y >= 0.0 ? 1e-5 : -1e-5) : rd.y,
        abs(rd.z) < 1e-5 ? (rd.z >= 0.0 ? 1e-5 : -1e-5) : rd.z
    );
    vec3 m = 1.0 / safeRd;
    vec3 n = m * ro;
    vec3 k = abs(m) * rad;
    vec3 t1 = -n - k;
    vec3 t2 = -n + k;
    float tN = max(max(t1.x, t1.y), t1.z);
    float tF = min(min(t2.x, t2.y), t2.z);
    if (tN > tF || tF < 0.0) return vec2(-1.0);
    return vec2(tN, tF);
}

void marchVolume(vec3 ro, vec3 rd, float near, float far, float time, inout vec3 color) {
    vec3 vColor = vec3(0.0);
    float visibility = 1.0;

    float inside = far - near;
    float stepSize = inside / VOLUME_STEPS;

    for (float t = near; t <= far; t += stepSize) {
        vec3 p = ro + t * rd;

        float s = CLOUDS_SMOOTHNESS * 0.01;
        float dens = smoothstep(-s, s, fbm3D(p + vec3(time * 0.055, time * 0.065, 1.0 + time * 0.02))) * 0.1;

        float prev = visibility;
        visibility *= exp(-stepSize * dens * ABSORPTION);

        float absorption = prev - visibility;
        float light = smoothstep(2.5, 6.5, p.z);
        vColor += absorption * dens * light * LIGHT_INTENSITY;
    }

    color = min(vColor, vec3(1.0)) + color * visibility;
}

vec3 renderShader1(vec2 fragCoord, float time, vec2 resolution) {
    vec2 uv = (fragCoord - resolution * 0.5) / resolution.y;
    vec3 ro = vec3(0.0, 0.0, 0.01) * 0.8;

    vec3 f = normalize(vec3(0.0, 0.0, 1.0) - ro * 0.05);
    vec3 r = normalize(cross(vec3(0.0, 1.0, 0.0), f));
    vec3 u = cross(f, r);
    vec3 c = ro + f;
    vec3 ip = c + uv.x * r + uv.y * u;
    vec3 rd = normalize(ip - ro);

    // Moon
    float moon = dot(rd, vec3(0.0, 0.0, 1.0));
    vec3 color = (vec3(0.7) - fbm3D(rd * 10.0 + vec3(43.12, 17.85, 91.24)) * 3.0) * vec3(smoothstep(0.995, 0.9955, moon));
    color += (vec3(1.25) - color) * pow(max(0.0, moon + 0.1), 6.0) * 0.25;

    // Dark grey ambient base
    color = mix(vec3(0.06, 0.06, 0.08), color, smoothstep(0.7, 0.998, moon));

    // Clouds volume march
    vec2 hit = boxIntersection(ro - vec3(0.0, 0.0, 4.0), rd);
    if (hit.x >= 0.0) {
        marchVolume(ro, rd, hit.x, hit.y, time, color);
    }

    // Tone mapping to match Dark Grey + White palette
    color = mix(vec3(0.06, 0.06, 0.08), vec3(0.95, 0.95, 0.98), clamp(color, 0.0, 1.0));
    return color;
}

// ----------------------------------------------------
// SHADER 2: Moonlight Raymarching by bµg
// ----------------------------------------------------

vec3 renderShader2(vec2 fragCoord, float time, vec2 resolution) {
    vec2 P = fragCoord;
    vec2 R = resolution;
    vec3 u = vec3((P + P - R.xy) / R.y, 1.0);

    vec3 o = vec3(0.0);
    vec3 p = vec3(0.0);
    float T = time;

    float t = 0.0;
    float d = 0.0;
    float m = 0.0;

    mat2 rot2 = mat2(8.0, 6.0, -6.0, 8.0) * 0.1;

    for (int i = 0; i < 90; i++) {
        p = normalize(u) * t;
        p.z -= 50.0;
        m = max(length(p) - 10.0, 0.01);
        p.z += T;

        vec4 cv = cos(t * 0.2 + vec4(0.0, 33.0, 11.0, 0.0));
        mat2 rot1 = mat2(cv.x, cv.y, cv.z, cv.w);
        p.xy = rot1 * p.xy;
        d = 5.0 - length(p.xy);

        float a = 0.01;
        for (int j = 0; j < 7; j++) {
            p.xz = rot2 * p.xz;
            d -= abs(dot(sin(p / a * 0.6 - T * 0.3), vec3(a)));
            m += abs(dot(sin(p / (a * 5.0)), vec3(a / 5.0)));
            a += a;
            if (a >= 1.0) break;
        }

        d = abs(d) * 0.15 + 0.1;
        vec3 colStep = vec3(1.0, 1.0, 1.05);

        if (t > 9.0) {
            d = 9.0;
            o += colStep / m + vec3(1.0);
        } else {
            o += colStep / m + colStep / d;
        }

        t += min(m, d);
    }

    o /= 400.0;
    vec3 val = o - o * length(u.xy * 0.5);

    vec3 finalCol = tanh_approx(mix(
        vec3(6.0, 6.0, 8.0),
        vec3(145.0, 145.0, 150.0),
        val
    ) * 0.01);

    return clamp(finalCol, 0.0, 1.0);
}

// ----------------------------------------------------
// MAIN: Interpolation between Shader 1 & Shader 2
// ----------------------------------------------------

void main() {
    float transition = clamp(Params.z, 0.0, 1.0);
    if (transition >= 1.0) {
        vec3 col2 = renderShader2(gl_FragCoord.xy, Params.x, OutSize);
        fragColor = vec4(clamp(col2, 0.0, 1.0), Params.y);
        return;
    }
    if (transition <= 0.0) {
        vec3 col1 = renderShader1(gl_FragCoord.xy, Params.x, OutSize);
        fragColor = vec4(clamp(col1, 0.0, 1.0), Params.y);
        return;
    }

    float smoothTransition = smoothstep(0.0, 1.0, transition);
    vec3 col1 = renderShader1(gl_FragCoord.xy, Params.x, OutSize);
    vec3 col2 = renderShader2(gl_FragCoord.xy, Params.x, OutSize);

    vec3 finalColor = mix(col1, col2, smoothTransition);
    fragColor = vec4(clamp(finalColor, 0.0, 1.0), Params.y);
}
