#version 300 es
precision mediump float;

layout (location = 1) in vec3 aPos;

out vec3 TexCoords;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    //view = mat4(mat3(view));
    TexCoords = aPos;
    gl_Position = projection * view * vec4(aPos, 1.0);
}