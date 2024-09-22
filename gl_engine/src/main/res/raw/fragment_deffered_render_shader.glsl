#version 320 es
precision mediump float;
in vec2 TexCoord;
out vec4 FragColor;
uniform sampler2D position_tex;
uniform sampler2D normal_tex;
uniform sampler2D albedo_tex;
void main()
{
    FragColor = vec4(texture(albedo_tex, TexCoord).rgb,1.0);
    //FragColor = vec4(0.3);
}