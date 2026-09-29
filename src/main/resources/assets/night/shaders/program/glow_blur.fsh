#version 330

// Shared real-blur pass for the Cozy menu's glow effects (hover text + embers) -- captures
// whatever immediate-mode content was drawn into a scratch RenderTarget (see
// CozyGlowCapture.java), box-blurs its alpha channel, and tints the result to a fixed warm
// colour. Same symmetric-kernel lesson as outline.fsh: loop bounds must be <=, not <, or the
// blur skews toward one diagonal.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform GlowConfig {
    vec4 GlowColor;
};

#define Width 6

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    float alpha = 0.0;
    float weight = 0.0;

    for (float x = -Width; x <= Width; x++) {
        for (float y = -Width; y <= Width; y++) {
            float d = length(vec2(x, y));
            if (d > float(Width)) continue;

            float w = 1.0 - d / float(Width);
            alpha += texture(InSampler, texCoord + vec2(x, y) * oneTexel).a * w;
            weight += w;
        }
    }

    alpha = weight > 0.0 ? alpha / weight : 0.0;
    fragColor = vec4(GlowColor.rgb, alpha * GlowColor.a);
}
