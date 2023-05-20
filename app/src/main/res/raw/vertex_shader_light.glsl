#version 300 es
precision mediump float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;//abs tangent
layout (location = 4) in vec3 aB;//absolute bitangent

out vec2 TexCoord;
out vec3 normal;
out mat4 model2;
out vec3 FragPos;

out vec3 TangentLightPos;
out vec3 TangentViewPos;
out vec3 TangentFragPos;


uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 viewPos;
//mat4 buff;

vec3 lightPos =  vec3(0.0, 3.0, 0.0);
void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    FragPos = (vec4(aPos, 1.0f)*transpose(model)).xyz;
    TexCoord = vec2(aTexCoord.x, aTexCoord.y);
    normal=normalize(normalVec);
    model2=model;
    vec3 T = normalize(vec3(model * vec4(aT, 0.0)));
    vec3 B = normalize(vec3(model * vec4(aB, 0.0)));
    vec3 N = normalize(vec3(model * vec4(normal, 0.0)));
    mat3 TBN = transpose(mat3(T, B, N));
    TangentLightPos = TBN * lightPos;
    TangentViewPos  = TBN * viewPos;
    TangentFragPos  = TBN * vec3(model * vec4(aPos, 1.0));
}