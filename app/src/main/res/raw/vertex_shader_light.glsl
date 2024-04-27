#version 320 es
precision mediump float;
precision mediump int;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;//abs tangent
layout (location = 4) in vec3 aB;//absolute bitangent

struct PointLight{
    vec3 lightPos;
    float specular;
    float diffuse;
    float ambinient;
};


out struct Data{
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;
out vec3 pLightPos[10];
out vec3 dLightDir[10];

struct AmibentLight {
    vec3 color;
};

// sun
struct DirectedLight {
    vec3 color;
    vec3 direction;
    float diffuse;
    float specular;
};

// flashlight
struct LightSource {
    vec3 color;
    vec3 direction;
    float angle;
};
uniform LightSource sLights[10];
uniform DirectedLight dLights[10];
uniform AmibentLight aLight;
uniform int pLightNumber;
uniform int sLightNum;
uniform int dLightNum;
uniform struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
} material;

uniform int pLightNum;
uniform PointLight pLights [10];
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 viewPos;

void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
    data.FragPos = (vec4(aPos, 1.0f)*transpose(model)).xyz;
    data.TexCoord = vec2(aTexCoord.x, aTexCoord.y);
    data.normal=normalize(normalVec);
    data.model2=model;
    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vec3 T = normalize(normalMatrix * aT);
    vec3 N = normalize(normalMatrix * data.normal);
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);
    mat3 TBN = transpose(mat3(T, B, N));
    for (int i=0;i<pLightNum;i++){
        pLightPos[i] = TBN * pLights[i].lightPos;
    }
    for (int i=0;i<dLightNum;i++){
        dLightDir[i] = TBN * dLights[i].direction;
    }
    data.TangentViewPos  = TBN * viewPos;
    data.TangentFragPos  = TBN * vec3(model * vec4(aPos, 1.0));
}