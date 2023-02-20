#version 300 es
precision mediump float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;

out vec2 TexCoord;
out vec3 normal;
out mat4 model2;
out vec3 FragPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
//mat4 buff;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    FragPos = (vec4(aPos, 1.0f)*transpose(model)).xyz;
    TexCoord = vec2(aTexCoord.x, aTexCoord.y);
    normal=normalVec;
    model2=model;
}