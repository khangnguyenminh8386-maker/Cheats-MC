#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MenuBgConfig {
    vec4 Params; // x: Time, y: Alpha, z: unused, w: unused
};

in vec2 texCoord;
out vec4 fragColor;

vec3 tanh_approx(vec3 x) {
    vec3 ex = exp(clamp(2.0 * x, -20.0, 20.0));
    return (ex - 1.0) / (ex + 1.0);
}

void main() {
    vec2 P = gl_FragCoord.xy;
    vec2 R = OutSize;
    vec3 u = vec3((P + P - R.xy) / R.y, 1.0);
    
    vec3 o = vec3(0.0);
    vec3 p = vec3(0.0);
    float T = Params.x;
    
    float t = 0.0;
    float d = 0.0;
    float m = 0.0;
    
    mat2 rot2 = mat2(8.0, 6.0, -6.0, 8.0) * 0.1;
    
    for (int i = 0; i < 90; i++) {
        p = normalize(u) * t;
        p.z -= 50.0;
        m = max(length(p) - 10.0, 0.01);
        p.z += T;
        
        vec4 cv = cos(t * 0.2 + vec4(0.0, 33.0, 11.0, 0.0));
        mat2 rot1 = mat2(cv.x, cv.y, cv.z, cv.w);
        p.xy = rot1 * p.xy;
        d = 5.0 - length(p.xy);
        
        float a = 0.01;
        for (int j = 0; j < 7; j++) {
            p.xz = rot2 * p.xz;
            d -= abs(dot(sin(p / a * 0.6 - T * 0.3), vec3(a)));
            m += abs(dot(sin(p / (a * 5.0)), vec3(a / 5.0)));
            a += a;
            if (a >= 1.0) break;
        }
        
        d = abs(d) * 0.15 + 0.1;
        
        // Pure silver-white moonlight raymarching (Dark Grey + White aesthetic)
        vec3 colStep = vec3(1.0, 1.0, 1.05);
        
        if (t > 9.0) {
            d = 9.0;
            o += colStep / m + vec3(1.0);
        } else {
            o += colStep / m + colStep / d;
        }
        
        t += min(m, d);
    }
    
    o /= 400.0;
    vec3 val = o - o * length(u.xy * 0.5);
    
    // Dark Grey + White tone mapping:
    // Deep graphite dark grey background sky -> Silvery velvety clouds -> Radiant white glowing moon
    vec3 finalCol = tanh_approx(mix(
        vec3(6.0, 6.0, 8.0),
        vec3(145.0, 145.0, 150.0),
        val
    ) * 0.01);
    
    fragColor = vec4(clamp(finalCol, 0.0, 1.0), Params.y);
}
