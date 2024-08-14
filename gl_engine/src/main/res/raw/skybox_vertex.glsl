#version 320 es
precision highp float;

layout (location = 1) in vec3 aPos;

out vec3 TexCoords;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    mat4 view2 = mat4(mat3(view));
    TexCoords = aPos;
    gl_Position = projection * view2 * vec4(aPos, 1.0);
}