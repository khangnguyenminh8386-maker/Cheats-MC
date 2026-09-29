#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

// 48 Vogel spiral Poisson-disk samples (R = 7.5 texels, Gaussian sigma = 3.0)
// Golden angle rotation ensures isotropic radial bloom without visible concentric rings, spokes, or banding.
const vec2 SAMPLES[48] = vec2[](
    vec2( 0.7655,  0.0000), vec2(-0.9776,  0.8956),
    vec2( 0.1496, -1.7051), vec2( 1.2322,  1.6072),
    vec2(-2.2613, -0.4000), vec2( 2.1421, -1.3626),
    vec2(-0.7165,  2.6653), vec2(-1.3664, -2.6310),
    vec2( 2.9646,  1.0827), vec2(-3.0842,  1.2731),
    vec2( 1.4868, -3.1771), vec2( 1.0987,  3.5028),
    vec2(-3.3114, -1.9191), vec2( 3.8847, -0.8540),
    vec2(-2.3708,  3.3722), vec2(-0.5477, -4.2266),
    vec2( 3.3624,  2.8338), vec2(-4.5247,  0.1871),
    vec2( 3.3004, -3.2844), vec2(-0.2208,  4.7752),
    vec2(-3.1404, -3.7632), vec2( 4.9747,  0.6693),
    vec2(-4.2150,  2.9327), vec2( 1.1518, -5.1198),
    vec2( 2.6640,  4.6491), vec2(-5.2079, -1.6615),
    vec2( 5.0588, -2.3373), vec2(-2.1916,  5.2367),
    vec2(-1.9560, -5.4381), vec2( 5.2046,  2.7354),
    vec2(-5.7810,  1.5239), vec2( 3.2860, -5.1104),
    vec2( 1.0453,  6.0822), vec2(-4.9537, -3.8364),
    vec2( 6.3367, -0.5250), vec2(-4.3800,  4.7347),
    vec2( 0.0319, -6.5401), vec2( 4.4540,  4.9099),
    vec2(-6.6883, -0.6199), vec2( 5.4195, -4.1132),
    vec2(-1.2331,  6.7779), vec2(-3.7142, -5.9023),
    vec2( 6.8063,  1.8653), vec2(-6.3522,  3.2598),
    vec2( 2.5103, -6.7710), vec2( 2.7532,  6.7632),
    vec2(-6.6707, -3.1614), vec2( 7.1297, -2.1982)
);

const float WEIGHTS[48] = float[](
    0.9680, 0.9070, 0.8498, 0.7962,
    0.7460, 0.6990, 0.6550, 0.6137,
    0.5750, 0.5388, 0.5048, 0.4730,
    0.4432, 0.4152, 0.3891, 0.3645,
    0.3416, 0.3200, 0.2999, 0.2810,
    0.2633, 0.2467, 0.2311, 0.2165,
    0.2029, 0.1901, 0.1781, 0.1669,
    0.1564, 0.1465, 0.1373, 0.1286,
    0.1205, 0.1129, 0.1058, 0.0991,
    0.0929, 0.0870, 0.0816, 0.0764,
    0.0716, 0.0671, 0.0629, 0.0589,
    0.0552, 0.0517, 0.0484, 0.0454
);

// Center weight = 1.0, sum of WEIGHTS = 14.3473, total = 15.3473
const float TOTAL_WEIGHT = 15.3473;

void main() {
    vec2 texSize = vec2(textureSize(Sampler0, 0));
    vec2 oneTexel = 1.0 / texSize;

    // Center sample
    float alphaSum = texture(Sampler0, texCoord0).a;

    // 48 Vogel spiral Poisson-disk taps (scaled by HD_SCALE for super-sampled font atlas)
    const float HD_SCALE = 2.0;
    for (int i = 0; i < 48; i++) {
        alphaSum += texture(Sampler0, texCoord0 + (SAMPLES[i] * HD_SCALE) * oneTexel).a * WEIGHTS[i];
    }

    float coverage = alphaSum / TOTAL_WEIGHT;

    if (coverage <= 0.001) {
        discard;
    }

    // Silky smooth Gaussian bloom falloff:
    // 1. Smooth feathering at outer tail guarantees a seamless, zero-derivative fade-to-transparent.
    // 2. Halo curve (pow 0.75) gives soft, luminous bloom radiance around text glyphs.
    float feather = smoothstep(0.001, 0.045, coverage);
    float halo = pow(clamp(coverage, 0.0, 1.0), 0.75);
    float glowAlpha = clamp(feather * halo * 1.15, 0.0, 1.0);

    if (glowAlpha <= 0.003) {
        discard;
    }

    // Boost luminous vibrance for a smooth neon light emission
    vec3 bloomRgb = clamp(vertexColor.rgb * 1.12 + vec3(0.03), 0.0, 1.0);

    fragColor = vec4(bloomRgb, glowAlpha * vertexColor.a) * ColorModulator;
}
