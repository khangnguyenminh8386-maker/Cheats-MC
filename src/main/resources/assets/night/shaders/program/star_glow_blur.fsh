#version 330

// Pass 1 of a separable (horizontal-then-vertical) blur -- see star_glow.fsh (pass 2) for why
// this got split. Pure 1D blur along Params.zw (a unit direction vector), no compositing.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform StarGlowConfig {
    vec4 Params;
};
#define Radius Params.y
#define Direction Params.zw

#define MAX_WIDTH 14

in vec2 texCoord;
out vec4 fragColor;

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

    fragColor = weight > 0.0 ? sum / weight : vec4(0.0);
}
