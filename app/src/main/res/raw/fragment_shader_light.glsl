#version 320 es
precision mediump float;
precision mediump int;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;
struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    float diffuse;
    float specular;
};

#define number 7

uniform struct AmibentLight {
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
    float cutOff;
    float outerCutOff;

    float constant;
    float linear;
    float quadratic;

    float ambient;
    float diffuse;
    float specular;
};
uniform SpotLight sLights[number];
uniform PointLight pLights[number];
uniform DirectedLight dLights[number];
uniform AmibentLight aLight;
uniform int pLightNumber;
uniform int sLightNumber;
uniform int sLightNum;
uniform int dLightNum;
uniform struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
} material;

in struct Data {
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;

in vec3 pLightPos[number];
in vec3 dLightDir[number];

in vec3 sLightDir[number];
in vec3 sLightPos[number];

vec3 applyAmbient(vec3 color) {
    return color * aLight.color;
}

vec3 applyDirectedLight(vec3 color, vec3 normal, vec3 viewDir, int index) {
    vec3 lightDir = normalize(-dLightDir[index]);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 reflectedDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectedDir), 0.0), material.shininess);
    vec3 diffuse = dLights[index].diffuse * diff * material.diffuse;
    vec3 specular = dLights[index].specular * spec * material.specular;
    return color * (specular + diffuse);
}


vec3 applyPointLight(vec3 color, int index, vec3 fragPos, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(pLightPos[index] - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(pLightPos[index] - fragPos);
    float attenuation = 1.0 / (pLights[index].constant + pLights[index].linear * distance +
    pLights[index].quadratic * (distance * distance));
    // combine results
    vec3 diffuse = pLights[index].diffuse * diff * material.diffuse;
    vec3 specular = pLights[index].specular * spec * material.specular;
    diffuse *= attenuation;
    specular *= attenuation;
    return color * (diffuse + specular);
}
// calculates the color when using a spot light.
vec3 CalcSpotLight(SpotLight light, vec3 normal, vec3 fragPos, vec3 viewDir, int i)
{
    vec3 lightDir = normalize(sLightPos[i] - fragPos);
    // diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);
    // specular shading
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    // attenuation
    float distance = length(sLightPos[i] - fragPos);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.quadratic * (distance * distance));
    // spotlight intensity
    float theta = dot(lightDir, normalize(-sLightDir[i]));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);
    // combine results
    vec3 ambient = light.ambient * material.diffuse;
    vec3 diffuse = light.diffuse * diff * material.diffuse;
    vec3 specular = light.specular * spec * material.specular;
    ambient *= attenuation * intensity;
    diffuse *= attenuation * intensity;
    specular *= attenuation * intensity;
    return (ambient + diffuse + specular);
    //return lightDir;
}

uniform int normalMapEnable;
void main()
{
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm;
    if (normalMapEnable == 1) {
        norm = normalize(texture(normalMap, data.TexCoord).rgb * 2.0 - 1.0);// god bless, everything is NORM
    } else {
        norm = normalize(vec3(0.0, 0.0, 1.0).rgb * 2.0 - 1.0);
    }
    vec3 result = applyAmbient(color);

    for (int i = 0; i < dLightNum; i++) {
        result += applyDirectedLight(color, norm, viewDir, i);
    }
    for (int i = 0;i < pLightNumber; i++) {
        result += applyPointLight(color, i, data.TangentFragPos, norm, viewDir);
    }
    for (int i = 0;i < sLightNumber; i++) {
        result += CalcSpotLight(sLights[i], norm, data.TangentFragPos, viewDir, i);
    }

    FragColor = vec4(result, 1.0);
}
