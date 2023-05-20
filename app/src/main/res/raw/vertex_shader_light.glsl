#version 300 es
precision mediump float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT; //abs tangent
layout (location = 4) in vec3 aB; //absolute bitangent

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
   /* vec3 T = normalize(vec3(model * vec4(aTangent,   0.0)));
    vec3 B = normalize(vec3(model * vec4(aBitangent, 0.0)));
    vec3 N = normalize(vec3(model * vec4(aNormal,    0.0)));
    mat3 TBN = mat3(T, B, N);
    */
}