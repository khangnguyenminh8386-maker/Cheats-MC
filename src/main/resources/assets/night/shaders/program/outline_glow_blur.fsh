#version 330

// Pass 1 of ShadersModule's Glow (Mode == "Glow"): horizontal half of a separable blur over the
// captured entity silhouette. Same shape as star_glow_blur.fsh (the existing precedent in this
// repo), one difference that matters:
//
// PREMULTIPLIED. The captured silhouette buffer only has meaningful .rgb where .a != 0 (empty
// pixels carry no color at all -- outline.fsh's own edge branch is careful to read rgb ONLY from
// texels with a != 0 for exactly this reason). Blurring rgb straight would drag those empty
// pixels' garbage/black into the halo's color. Blurring (rgb * a, a) instead and dividing by the
// blurred alpha at composite time is the standard fix, and it makes the blurred .a fall out as
// exactly the coverage number the composite pass wants.
//
// Replaces a single-pass fixed 9x9 tap grid whose stride grew with radius (up to ~8 texels between
// taps at max intensity) -- that read as discrete concentric bands, not a glow. Undersampling, not
// a falloff-curve problem: no weight tuning fixes 9 samples spread over 32 texels. Unit-texel
// steps here, so the sampling is dense by construction at every radius.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform OutlineGlowConfig {
    vec4 Params;
    vec4 GlowColor;
};
#define Radius Params.y
#define Direction Params.zw

// Fixed loop bound (GLSL 330 needs one) -- and the loop body runs all 49 iterations regardless of
// the runtime Radius, `continue` only skips the fetch. Kept at the top of the radius ramp
// EspShader#writeOutlineSettings can produce; do not raise one without the other.
#define MAX_WIDTH 24

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
        vec4 texel = texture(InSampler, texCoord + Direction * i * oneTexel);
        sum += vec4(texel.rgb * texel.a, texel.a) * w;
        weight += w;
    }

    fragColor = weight > 0.0 ? sum / weight : vec4(0.0);
}
