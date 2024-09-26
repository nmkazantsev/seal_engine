#version 320 es
precision mediump float;
in vec2 TexCoord;
out vec4 FragColor;
layout (location =0)uniform sampler2D N;
layout (location =1)uniform sampler2D A;
layout (location =2)uniform sampler2D P;

#define  snumber 10//spot number
#define  dnumber 10//direct number
#define  pnumber 10//point number


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

vec3 applyAmbient(vec3 color) {
    return color * aLight.color;
}

vec3 applyDirectedLight(vec3 color, vec3 normal, vec3 viewDir, int index) {
    vec3 lightDir = normalize(dLights[index].direction);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectedDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectedDir), 0.0), material.shininess);
    vec3 diffuse = dLights[index].diffuse * diff * material.diffuse;
    vec3 specular = dLights[index].specular * spec * material.specular;
    return color *dLights[index].color* (diffuse+specular);
}


vec3 applyPointLight(vec3 color, int index, vec3 fragPos, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(pLights[index].position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(pLights[index].position - fragPos);
    float attenuation = 1.0 / (pLights[index].constant + pLights[index].linear * distance +
    pLights[index].quadratic * (distance * distance));
    // combine results
    vec3 diffuse = pLights[index].diffuse * diff * material.diffuse;
    vec3 specular = pLights[index].specular * spec * material.specular;
    diffuse *= attenuation;
    specular *= attenuation;
    return color *pLights[index].color* (diffuse + specular);
}

// calculates the color when using a spot light.
vec3 CalcSpotLight(vec3 color, SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, int i)
{
    vec3 lightDir = normalize(sLights[i].position - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(sLights[i].position - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    // spotlight intensity
    float theta = dot(lightDir, normalize(-sLights[i].direction));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    // combine results
    vec3 ambient = light.ambient * material.diffuse;
    vec3 diffuse = light.diffuse * diff * material.diffuse;
    vec3 specular = light.specular * spec * material.specular;
    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;
    return (ambient + diffuse + specular)*color*light.color;
}

void main()
{
    vec3 color = texture(A, TexCoord).rgb;
    vec3 norm = texture(N, TexCoord).rgb;
    vec3 position = texture(P, TexCoord).rgb;
    vec3 viewDir=normalize(viewPos-position);
    vec3 result = applyAmbient(color);
    for (int i = 0; i < dLightNum; i++) {
        result += applyDirectedLight(color, norm, viewDir, i);
    }
    for (int i = 0;i < pLightNum; i++) {
        result += applyPointLight(color, i, position, norm, viewDir);
    }
    for (int i = 0;i < sLightNum; i++) {
        result += CalcSpotLight(color, sLights[i], norm, position, viewDir, i);
    }
    FragColor = vec4(result, 0.0);
}