#version 320 es
precision mediump float;
in vec2 TexCoord;
out vec4 FragColor;
layout (location =0)uniform sampler2D N;
layout (location =1)uniform sampler2D A;
layout (location =2)uniform sampler2D P;

#define  snumber 1//spot number
#define  dnumber 1//direct number
#define  pnumber 1//point number

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;//abs tangent
layout (location = 4) in vec3 aB;//absolute bitangent

struct PointLight {
    vec3 position;
    vec3 color;
    float constant;
    float linear;
    float quadratic;

    float diffuse;
    float specular;
};

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
struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    float ambient;
    float diffuse;
    float specular;
};
uniform SpotLight sLights[snumber];
uniform DirectedLight dLights[dnumber];
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
uniform PointLight pLights[pnumber];
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 viewPos;
uniform mat4 lightProjectionMatrix;
uniform mat4 lightViewMatrix;

void main()
{
    FragColor = vec4(sLights[0].position.xyz,0.0)/2.0;//vec4(texture(P, TexCoord).rgb*0.1+texture(N, TexCoord).rgb*0.0+texture(P, TexCoord).rgb*0.0, 1.0);
    //FragColor = vec4(0.3);
}