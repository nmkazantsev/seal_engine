#version 300 es
precision mediump float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;//abs tangent
layout (location = 4) in vec3 aB;//absolute bitangent

out struct Data{
    vec3 normal;
    mat4 model2;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentLightPos;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 viewPos;


vec3 lightPos =  vec3(0.0, 3.0, 0.0);
void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    data.FragPos = (vec4(aPos, 1.0f)*transpose(model)).xyz;
    data.TexCoord = vec2(aTexCoord.x, aTexCoord.y);
    data.normal=normalize(normalVec);
    data.model2=model;
    vec3 T = normalize(vec3(model * vec4(aT, 0.0)));
    vec3 B = normalize(vec3(model * vec4(aB, 0.0)));
    vec3 N = normalize(vec3(model * vec4(data.normal, 0.0)));
    mat3 TBN = transpose(mat3(T, B, N));
    data.TangentLightPos = TBN * lightPos;
    data.TangentViewPos  = TBN * viewPos;
    data.TangentFragPos  = TBN * vec3(model * vec4(aPos, 1.0));
}