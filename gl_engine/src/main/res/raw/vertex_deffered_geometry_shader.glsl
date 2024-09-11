#version 300 es
precision mediump float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;//abs tangent
layout (location = 4) in vec3 aB;//absolute bitangent

out vec2 TexCoord;
out vec3 FragPos;
out vec3 normal;
out vec3 aTv;
out vec3 aBv;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    FragPos = (model* vec4(aPos, 1.0f)).xyz;
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    TexCoord = vec2(aTexCoord.x, aTexCoord.y);
    mat3 normalMatrix = inverse(mat3(model));
    normal = normalize(normalVec*normalMatrix);
    aTv = normalize(aT*normalMatrix);
    aTv = normalize(aTv - dot(aTv, normal) * normal);
    aBv = normalize(cross(normal, aTv));
}