#version 320 es
precision mediump float;
precision mediump int;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;
/*
struct PointLight2 {
    vec3 lightPos;
    vec3 color;
    vec3 ambinient;
};
*/
//uniform PointLight2 pLights2[10];
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

in struct Data {
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;
in vec3 pLightPos[10];
in vec3 dLightDir[10];


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
    return color* (specular+diffuse);
}

vec3 applySourceLight(vec3 color, int index) { return vec3(0.0); }

vec3 applyPointLight(vec3 color, int index) { return vec3(0.0); }

void main()
{
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm = normalize(texture(normalMap, data.TexCoord).rgb * 2.0 - 1.0);// god bless, everything is norm
    vec3 result = applyAmbient(color);

    for (int i = 0; i < dLightNum; i++) {
        result += applyDirectedLight(color, norm, viewDir, i);
    }

    FragColor=vec4(result, 1.0);
}
