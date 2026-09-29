#version 150

out vec4 fragColor;

uniform float u_Time;
uniform vec4 u_Color1;
uniform vec4 u_Color2;
uniform float u_Speed;
uniform vec2 u_Resolution;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

const mat2 ROT = mat2(0.8775826, 0.4794255, -0.4794255, 0.8775826);

float fbm(vec2 pos) {
    float v = 0.0;
    float a = 0.5;
    float speed = u_Time * u_Speed;
    for (int i = 0; i < 6; i++) {
        float dir = float(i & 1) * 2.0 - 1.0;
        v += a * noise(pos - 0.05 * dir * speed);
        pos = ROT * pos * 2.0 + 100.0;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec2 p = (gl_FragCoord.xy * 2.0 - u_Resolution.xy) / min(u_Resolution.x, u_Resolution.y) - vec2(12.0, 0.0);

    vec2 q = vec2(fbm(p), fbm(p + 1.0));
    float f = fbm(p + q);

    vec3 color = mix(
        vec3(0.3, 0.3, 0.3),
        vec3(0.7, 0.7, 0.7),
        clamp(f * f * 4.0, 0.0, 1.0)
    );

    color = mix(color, vec3(0.7, 0.7, 0.7), clamp(length(q), 0.0, 1.0));

    color = (f * f * f + 0.9 * f * f + 0.8 * f) * color;

    fragColor = u_Color1 + (u_Color2 - u_Color1) * color.r;
}
