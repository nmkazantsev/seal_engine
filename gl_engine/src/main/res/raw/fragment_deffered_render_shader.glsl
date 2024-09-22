#version 320 es
precision mediump float;
in vec2 TexCoord;
out vec4 FragColor;
layout (location =0)uniform sampler2D N;
layout (location =1)uniform sampler2D A;
layout (location =2)uniform sampler2D P;
void main()
{
    FragColor = vec4(texture(A, TexCoord).rgb*0.0+texture(N, TexCoord).rgb+texture(P, TexCoord).rgb*0.0, 1.0);
    //FragColor = vec4(0.3);
}