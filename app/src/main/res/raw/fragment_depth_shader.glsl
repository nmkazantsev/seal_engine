#version 300 es
precision mediump float;
layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec4 gAlbedoSpec;

in vec2 TexCoord;
uniform sampler2D textureSamp;
void main()
{
   // FragColor = texture(textureSamp, TexCoord);

   // FragColor = vec4(vec3(gl_FragCoord.z),  texture(textureSamp, TexCoord).x);
    gNormal = vec3(gl_FragCoord.z),  texture(textureSamp, TexCoord).x;
}