#version 330

// Pass 2: second half of the separable blur (vertical, over the already horizontally-blurred
// input from star_glow_blur.fsh), then composite with the sharp original.
//
// 2026-08-19 PERF FIX (reported: Star Glow dropped FPS 120 -> 30 regardless of Intensity). The
// single-pass version here used to be a full O(radius^2) nested loop -- MAX_WIDTH 14 meant a
// 29x29 = 841-sample loop PER PIXEL, at full screen resolution, every frame, with the loop bound
// fixed at compile time regardless of the runtime Radius/Intensity value (the `if (d > Radius)
// continue` only skips the texture fetch, not the loop iteration itself -- 841 iterations of
// counter/distance-compute/branch always ran). Separated into two O(radius) 1D passes (this file
// + star_glow_blur.fsh) -- 29 + 29 = 58 samples/pixel total, ~14.5x fewer than before, and now
// actually independent of screen-space direction cost blowing up quadratically with radius.

uniform sampler2D InSampler;
uniform sampler2D OriginalSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform StarGlowConfigV {
    vec4 Params;
};
#define Intensity Params.x
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

    vec4 blurred = weight > 0.0 ? sum / weight : vec4(0.0);
    vec4 original = texture(OriginalSampler, texCoord);

    // max(original, blurred * Intensity) clamps to [0,1] on write regardless of how far past 1.0
    // blurred*Intensity goes -- past roughly Intensity~5 the halo is already pure white, so a
    // 20-vs-1000 slider was doing nothing visible (root cause of "not much difference"). Radius
    // (driven by Intensity too, see EspShader#writeStarGlowSettings) is what actually keeps
    // growing the visible effect once brightness has maxed out -- a wider, more diffuse halo
    // instead of an invisible brightness increase past white.
    fragColor = vec4(max(original.rgb, blurred.rgb * Intensity), max(original.a, blurred.a));
}
