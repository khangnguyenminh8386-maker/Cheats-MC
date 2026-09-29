#version 330

// ClickGUI Multi-Mode Procedural Background Shader for Night Client (26.2 Mojang Mappings)
// Supports: Aurora, Cyber Grid, Constellations, Mesh Glow, Dark Frost

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform ClickGuiBgConfig {
    vec4 Params;     // x: Time, y: Mode (0..4), z: Opacity (0..1), w: Speed
    vec4 ThemeColor; // rgb: Primary theme color, a: Rainbow flag (1.0 = rainbow)
};

#define Time (Params.x * Params.w)
#define Mode int(Params.y + 0.5)
#define Opacity Params.z
#define Resolution OutSize

in vec2 texCoord;
out vec4 fragColor;

// ---------------------------------------------------------------- Utils

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

vec2 hash2(vec2 p) {
    return vec2(
        hash(p),
        hash(p + vec2(17.1, 31.7))
    );
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(p);
        p = rot * p * 2.0 + vec2(100.0);
        a *= 0.5;
    }
    return v;
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 getBaseColor() {
    if (ThemeColor.a > 0.5) {
        return hsv2rgb(vec3(fract(Time * 0.1), 0.75, 0.95));
    }
    return ThemeColor.rgb;
}

// ---------------------------------------------------------------- Mode 0: Aurora Flow

vec4 renderAurora(vec2 uv) {
    vec2 p = (gl_FragCoord.xy - 0.5 * Resolution) / Resolution.y;
    vec3 baseCol = getBaseColor();
    vec3 secCol = mix(baseCol, vec3(0.1, 0.8, 0.9), 0.5);

    float t = Time * 0.5;
    float wave1 = sin(p.x * 2.5 + t + sin(p.y * 3.0 + t * 0.7)) * 0.5;
    float wave2 = cos(p.x * 3.8 - t * 0.8 + cos(p.y * 2.5 - t * 0.5)) * 0.5;
    float wave3 = sin(p.x * 1.5 + p.y * 1.5 + t * 0.4) * 0.5;

    float d1 = abs(p.y - wave1 * 0.35 - 0.1);
    float d2 = abs(p.y - wave2 * 0.45 + 0.1);
    float d3 = abs(p.y - wave3 * 0.3);

    float glow1 = 0.035 / (d1 + 0.04);
    float glow2 = 0.030 / (d2 + 0.04);
    float glow3 = 0.025 / (d3 + 0.05);

    vec3 col = baseCol * glow1 + secCol * glow2 + vec3(0.5, 0.3, 0.9) * glow3;
    col += baseCol * 0.08 * (1.0 - length(p * 0.8));

    float alpha = clamp((glow1 + glow2 + glow3) * 0.6, 0.0, 1.0) * Opacity;
    return vec4(col, alpha);
}

// ---------------------------------------------------------------- Mode 1: Cyber Grid

vec4 renderCyberGrid(vec2 uv) {
    vec2 p = (gl_FragCoord.xy - 0.5 * Resolution) / Resolution.y;
    vec3 baseCol = getBaseColor();

    float horizon = -0.15;
    vec3 col = vec3(0.02, 0.02, 0.05);

    if (p.y > horizon) {
        // Sky with stars and horizon glow
        float skyY = p.y - horizon;
        float horizonGlow = exp(-skyY * 4.0);
        col += baseCol * horizonGlow * 0.6;

        vec2 starPos = p * 18.0;
        float star = hash(floor(starPos));
        if (star > 0.97) {
            float blink = sin(Time * 3.0 + star * 6.28) * 0.5 + 0.5;
            col += vec3(0.8, 0.9, 1.0) * blink * 0.5;
        }
    } else {
        // 3D Perspective Grid
        float depth = 0.35 / (horizon - p.y);
        vec2 gridP = vec2(p.x * depth, depth - Time * 2.0);

        vec2 grid = abs(fract(gridP - 0.5) - 0.5) / fwidth(gridP);
        float line = min(grid.x, grid.y);
        float gridAlpha = 1.0 - min(line, 1.0);

        float distFade = clamp(1.0 - (horizon - p.y) * 1.5, 0.0, 1.0);
        distFade = exp(-distFade * 2.0);

        vec3 gridCol = mix(baseCol, vec3(0.2, 0.9, 1.0), sin(gridP.y * 0.2 + Time) * 0.5 + 0.5);
        col += gridCol * gridAlpha * distFade * 0.8;
        col += baseCol * 0.15 * distFade;
    }

    return vec4(col, Opacity * 0.85);
}

// ---------------------------------------------------------------- Mode 2: Constellations

vec4 renderConstellations(vec2 uv) {
    vec2 p = gl_FragCoord.xy / Resolution.y;
    vec3 baseCol = getBaseColor();
    vec3 col = vec3(0.0);

    float totalAlpha = 0.0;
    vec2 cell = floor(p * 7.0);
    vec2 f = fract(p * 7.0);

    vec2 points[9];
    int idx = 0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 g = vec2(float(x), float(y));
            vec2 h = hash2(cell + g);
            vec2 pos = g + sin(Time * 0.4 + h * 6.28) * 0.35 + 0.5;
            points[idx++] = pos;
        }
    }

    // Draw nodes & connections
    vec2 currentPos = f;
    for (int i = 0; i < 9; i++) {
        float d = length(currentPos - points[i]);
        float nodeGlow = 0.015 / (d + 0.02);
        col += baseCol * nodeGlow * 0.4;
        totalAlpha += nodeGlow * 0.2;

        for (int j = i + 1; j < 9; j++) {
            vec2 pa = currentPos - points[i];
            vec2 ba = points[j] - points[i];
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            float lineDist = length(pa - ba * h);
            float lineLen = length(ba);
            if (lineLen < 1.4) {
                float lineGlow = smoothstep(0.04, 0.0, lineDist) * (1.0 - lineLen / 1.4);
                col += mix(baseCol, vec3(0.8, 0.9, 1.0), 0.3) * lineGlow * 0.35;
                totalAlpha += lineGlow * 0.25;
            }
        }
    }

    float bgGlow = (1.0 - length((gl_FragCoord.xy - 0.5 * Resolution) / Resolution.y)) * 0.1;
    col += baseCol * bgGlow;

    return vec4(col, clamp(totalAlpha + bgGlow, 0.0, 1.0) * Opacity);
}

// ---------------------------------------------------------------- Mode 3: Mesh Glow

vec4 renderMeshGlow(vec2 uv) {
    vec2 p = (gl_FragCoord.xy - 0.5 * Resolution) / Resolution.y;
    vec3 baseCol = getBaseColor();

    float t = Time * 0.6;
    vec2 blob1 = vec2(sin(t * 0.7) * 0.4, cos(t * 0.9) * 0.25);
    vec2 blob2 = vec2(cos(t * 0.8) * 0.45, sin(t * 0.6) * 0.3);
    vec2 blob3 = vec2(sin(t * 1.1 + 1.5) * 0.3, cos(t * 0.5 + 2.0) * 0.35);

    float d1 = length(p - blob1);
    float d2 = length(p - blob2);
    float d3 = length(p - blob3);

    float g1 = exp(-d1 * 2.8);
    float g2 = exp(-d2 * 3.2);
    float g3 = exp(-d3 * 2.5);

    vec3 c1 = baseCol;
    vec3 c2 = mix(baseCol, vec3(0.2, 0.6, 1.0), 0.6);
    vec3 c3 = mix(baseCol, vec3(0.9, 0.3, 0.6), 0.7);

    vec3 col = c1 * g1 + c2 * g2 + c3 * g3;
    float noiseGrain = (hash(gl_FragCoord.xy) - 0.5) * 0.03;
    col += noiseGrain;

    float alpha = clamp((g1 + g2 + g3) * 0.6, 0.0, 1.0) * Opacity;
    return vec4(col, alpha);
}

// ---------------------------------------------------------------- Mode 4: Dark Frost

vec4 renderDarkFrost(vec2 uv) {
    vec2 p = (gl_FragCoord.xy - 0.5 * Resolution) / Resolution.y;
    vec3 baseCol = getBaseColor();

    float dist = length(p);
    float centerGlow = exp(-dist * 1.8) * 0.35;
    float vignette = smoothstep(1.2, 0.3, dist);

    float f = fbm(p * 2.5 + vec2(Time * 0.05));
    vec3 col = mix(vec3(0.03, 0.03, 0.06), baseCol * 0.4, centerGlow + f * 0.1);
    col *= vignette;

    return vec4(col, Opacity * 0.75);
}

// ---------------------------------------------------------------- Main

void main() {
    if (Opacity <= 0.001) {
        fragColor = vec4(0.0);
        return;
    }

    vec4 result;
    if (Mode == 0) {
        result = renderAurora(texCoord);
    } else if (Mode == 1) {
        result = renderCyberGrid(texCoord);
    } else if (Mode == 2) {
        result = renderConstellations(texCoord);
    } else if (Mode == 3) {
        result = renderMeshGlow(texCoord);
    } else {
        result = renderDarkFrost(texCoord);
    }

    fragColor = result;
}
