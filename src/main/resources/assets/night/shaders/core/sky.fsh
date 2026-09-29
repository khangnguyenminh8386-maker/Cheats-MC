#version 330

out vec4 fragColor;

layout(std140) uniform SkySettings {
    vec4 Tint;
    vec4 Color1;
    vec4 Color2;
    vec4 CameraAngles;
    vec2 Resolution;
    float Time;
    float Intensity;
    float Speed;
    int Mode;
};

/* ================= ULTRA-SMOOTH GRADIENT NOISE (NO GRID ARTIFACTS) ================= */

vec2 hashGrad(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    // Quintic C2 continuous interpolation - perfectly smooth, eliminating all box / square boundaries
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);

    float n00 = dot(hashGrad(i + vec2(0.0, 0.0)), f - vec2(0.0, 0.0));
    float n10 = dot(hashGrad(i + vec2(1.0, 0.0)), f - vec2(1.0, 0.0));
    float n01 = dot(hashGrad(i + vec2(0.0, 1.0)), f - vec2(0.0, 1.0));
    float n11 = dot(hashGrad(i + vec2(1.0, 1.0)), f - vec2(1.0, 1.0));

    return 0.5 + 0.5 * mix(mix(n00, n10, u.x), mix(n01, n11, u.x), u.y);
}

const mat2 ROT = mat2(0.80, 0.60, -0.60, 0.80);

float fbm_nebula(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = ROT * p * 2.02 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

float fbm_smoke(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    float speed = Time * Speed;
    for (int i = 0; i < 6; i++) {
        float dir = float(i & 1) * 2.0 - 1.0;
        v += a * noise(pos - 0.05 * dir * speed);
        pos = ROT * pos * 2.02 + 100.0;
        a *= 0.5;
    }
    return v;
}

/* ================= MAIN ================= */

void main() {
    /* Pure 2D screen-space fixed coordinate (does not rotate with camera) */
    vec2 p = (gl_FragCoord.xy - 0.5 * Resolution.xy) / Resolution.y;

    if (Mode == 1) {
        /* ---------- Smoke ---------- */
        vec2 sp = p * 2.5 - vec2(12.0, 0.0);

        vec2 q = vec2(fbm_smoke(sp), fbm_smoke(sp + 1.0));
        float f = fbm_smoke(sp + q);

        vec3 color = mix(
            vec3(0.3, 0.3, 0.3),
            vec3(0.7, 0.7, 0.7),
            clamp(f * f * 4.0, 0.0, 1.0)
        );

        color = mix(color, vec3(0.7, 0.7, 0.7), clamp(length(q), 0.0, 1.0));
        color = (f * f * f + 0.9 * f * f + 0.8 * f) * color;

        fragColor = Color1 + (Color2 - Color1) * color.r;
    } else {
        /* ---------- Nebula ---------- */
        p *= clamp(Intensity, 0.6, 1.4);

        vec2 np = p * 1.5 - vec2(Time * 0.04, sin(Time * 0.13) * 0.1);
        float n  = fbm_nebula(np * 1.5);
        float n2 = fbm_nebula(np * 3.0 + vec2(5.0, 2.0));

        float neb = smoothstep(0.15, 0.85, n * 0.8 + n2 * 0.4);

        vec3 nebColor = mix(
            vec3(0.05, 0.04, 0.12),
            vec3(0.7, 0.35, 0.9),
            pow(neb, 1.4)
        );

        float core = exp(-length(p) * 2.2);
        vec3 coreColor = vec3(1.0, 0.9, 0.7) * core * 0.6;

        vec3 col = vec3(0.0);
        col += nebColor * neb * 0.85;
        col += coreColor;

        col = 1.0 - exp(-col);
        col = pow(col, vec3(1.0 / 2.2));
        col *= Tint.rgb;

        fragColor = vec4(col, 1.0);
    }
}
