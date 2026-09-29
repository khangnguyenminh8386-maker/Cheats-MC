#version 330

#define TAU 6.28318530718

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BloomConfig {
    vec4 Params0;
    vec4 Params1;
    vec4 GradientColor;
    vec4 TimeRes;
};

#define u_Width Params0.x
#define u_GlowInside int(Params0.y)
#define u_GlowQuality int(Params0.z)
#define u_GlowMultiplier Params0.w

#define u_FillAlpha Params1.x
#define u_OutlineAlpha Params1.y
#define u_FillMode int(Params1.z)
#define u_GradientFactor Params1.w

#define u_GradientColor GradientColor
#define u_ShaderTime TimeRes.x
#define u_Resolution OutSize

in vec2 texCoord;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec4 getFill(vec3 centerColor, float alpha) {
    if (u_FillMode == 1) {
        float time = u_ShaderTime / 5.0;
        float distance = sqrt(gl_FragCoord.x * gl_FragCoord.x + gl_FragCoord.y * gl_FragCoord.y) + time;
        distance = distance / u_GradientFactor;
        distance = ((sin(distance) + 1.0) / 2.0);
        float j = 1.0 - distance;
        float r = centerColor.r * distance + u_GradientColor.r * j;
        float g = centerColor.g * distance + u_GradientColor.g * j;
        float b = centerColor.b * distance + u_GradientColor.b * j;
        float a = alpha * distance + u_GradientColor.a * j;
        return vec4(r, g, b, a);
    }

    if (u_FillMode == 4) {
        float zoom = 1.0;
        vec2 uv = (gl_FragCoord.xy / u_Resolution.xy - 0.5) * zoom + 0.5;
        float theta = uv.x * 3.14159;
        float phi = uv.y * 3.14159 * 0.5;
        vec3 dir = vec3(cos(phi) * cos(theta), sin(phi), cos(phi) * sin(theta));
        float time = u_ShaderTime / 750.0;
        float rot = time * 0.2;
        mat2 rotMat = mat2(cos(rot), -sin(rot), sin(rot), cos(rot));
        dir.xz = rotMat * dir.xz;
        float dist = length(dir.xy);
        float angle = atan(dir.y, dir.x);
        float spiral = sin(dist * 10.0 - angle * 3.0 - time * 2.0);
        float hue = fract(dist * 2.0 - time * 0.3 + angle / 6.28318);
        vec3 rainbowColor = hsv2rgb(vec3(hue, 0.8, 1.0));
        float rings = sin(dist * 20.0 - time * 3.0);
        rings = pow(max(0.0, rings), 3.0);
        vec3 finalColor = rainbowColor * (spiral * 0.3 + 0.7);
        finalColor += vec3(1.0) * rings * 0.5;
        float glow = exp(-dist * 3.0);
        finalColor += vec3(1.0, 0.9, 1.0) * glow * 0.5;
        return vec4(finalColor, alpha);
    }

    return vec4(centerColor, alpha);
}

float blur(vec2 uv, vec2 oneTexel)
{
    if (u_Width < 0.5) return 0.0;

    int step = max(u_GlowQuality, 1);
    int w = int(max(u_Width, 1.0)) * step;
    if (w == 0) return 0.0;

    float blurred = 0.0;
    float totalWeight = 0.0;

    for (int x = -w; x <= w; x += step) {
        for (int y = -w; y <= w; y += step) {
            float dist = length(vec2(float(x), float(y)));
            if (dist > float(w)) continue;

            float weight = exp(-dist * dist / (float(w) * float(w)));
            blurred += smoothstep(0.01, 0.5, texture(InSampler, uv + oneTexel * vec2(float(x), float(y))).a) * weight;
            totalWeight += weight;
        }
    }

    return clamp(blurred / max(totalWeight, 0.0001), 0.0, 1.0) * u_GlowMultiplier;
}

void main()
{
    vec2 oneTexel = 1.0 / InSize;
    vec4 center = texture(InSampler, texCoord);

    if (center.a > 0.0)
    {
        vec4 fill = getFill(center.rgb, u_FillAlpha);
        if (u_GlowInside == 1)
        {
            vec4 outline = vec4(getFill(center.rgb, 1.0).rgb, u_OutlineAlpha);
            fragColor = mix(fill, outline, clamp(u_GlowMultiplier - blur(texCoord, oneTexel), 0.0, 1.0));
        } else {
            fragColor = fill;
        }
        return;
    }

    float glow = blur(texCoord, oneTexel);
    if (glow < 0.001)
    {
        discard;
    }

    vec3 outlineRGB = vec3(0.0);
    bool edge = false;
    int maxR = int(max(u_Width, 1.0)) * max(u_GlowQuality, 1);

    for (int r = 1; r <= maxR; r += 1) {
        vec2 dx = vec2(oneTexel.x * float(r), 0.0);
        vec2 dy = vec2(0.0, oneTexel.y * float(r));

        vec4 s = texture(InSampler, texCoord - dx);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord + dx);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord - dy);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord + dy);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        vec2 od = vec2(dx.x, dy.y);
        s = texture(InSampler, texCoord + od);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord + vec2(od.x, -od.y));
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord + vec2(-od.x, od.y));
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }

        s = texture(InSampler, texCoord - od);
        if (s.a > 0.0) { outlineRGB = s.rgb; if (r == 1) edge = true; break; }
    }

    vec3 fillOutline = getFill(outlineRGB, 1.0).rgb;
    if (edge) {
        fragColor = vec4(fillOutline, u_OutlineAlpha);
    } else {
        fragColor = vec4(fillOutline, glow * u_OutlineAlpha);
    }
}
