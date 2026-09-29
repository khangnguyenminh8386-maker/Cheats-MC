#version 150

out vec4 fragColor;

/* ================= UNIFORMS ================= */

uniform vec3  u_Tint;
uniform vec2  u_Resolution;
uniform float u_Intensity;
uniform float u_Time;

/* ================= NOISE ================= */

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p){
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i);
    float b = hash(i + vec2(1.0,0.0));
    float c = hash(i + vec2(0.0,1.0));
    float d = hash(i + vec2(1.0,1.0));
    vec2 u = f*f*(3.0-2.0*f);
    return mix(a, b, u.x) + (c - a)*u.y*(1.0 - u.x) + (d - b)*u.x*u.y;
}

float fbm(vec2 p){
    float v = 0.0;
    float amp = 0.5;
    for(int i = 0; i < 5; i++){
        v += amp * noise(p);
        p *= 2.0;
        amp *= 0.5;
    }
    return v;
}

/* ================= MAIN ================= */

void main() {

    /* ---------- Screen space ---------- */
    vec2 uv = gl_FragCoord.xy / u_Resolution.xy;

    vec2 p = (gl_FragCoord.xy - 0.5 * u_Resolution.xy) / u_Resolution.y;
    p *= clamp(u_Intensity, 0.6, 1.4);

    /* ---------- Nebula ---------- */
    vec2 np = p * 1.5 - vec2(u_Time * 0.04, sin(u_Time * 0.13) * 0.1);
    float n  = fbm(np * 1.5);
    float n2 = fbm(np * 3.0 + vec2(5.0, 2.0));

    float neb = smoothstep(0.25, 0.8, n * 0.8 + n2 * 0.4);

    vec3 nebColor = mix(
        vec3(0.05, 0.04, 0.12),
        vec3(0.7, 0.35, 0.9),
        pow(neb, 1.4)
    );

    /* ---------- Core ---------- */
    float core = exp(-length(p) * 2.2);
    vec3 coreColor = vec3(1.0, 0.9, 0.7) * core * 0.6;

    /* ---------- Combine ---------- */
    vec3 col = vec3(0.0);
    col += nebColor * neb * 0.85;
    col += coreColor;

    /* ---------- Post ---------- */
    col = 1.0 - exp(-col);
    col = pow(col, vec3(1.0 / 2.2));
    col *= u_Tint;

    fragColor = vec4(col, 1.0);
}
