#version 330

// DeathEffects' Meme mode -- ported verbatim from example-addon-master's killeffect_logo.fsh
// (shared there by both Logo and Meme; only Meme is ported here). Reuses vanilla's own
// core/entity.vsh (via RenderPipelinesAccessor's ENTITY_SNIPPET/GLOBALS_SNIPPET), just samples
// Sampler0 (meme_circle.png / meme_arrow.png) with REAL alpha compositing (TRANSLUCENT blend
// target, not additive) so the billboard reads as a solid textured card instead of a glow smear.

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;


uniform sampler2D Sampler0;
out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
    if (texColor.a < 0.01) discard;
    vec4 color = texColor * vertexColor * ColorModulator;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
