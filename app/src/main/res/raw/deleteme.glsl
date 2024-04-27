#version 300 es
precision mediump float;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

struct PointLight {
    vec3 lightPos;
    vec3 color;
};

uniform PointLight pLights[10];

struct AmibentLight {
    vec3 color;
};

uniform AmibientLight aLight;

// sun
struct DirectedLight {
    vec3 color;
    vec3 direction;
    float diffuse;
    float specular;
};

uniform DirectedLight dLights[10];

// flashlight
struct LightSource {
    vec3 color;
    vec3 direction;
    float angle;
};

uniform LightSource sLights[10];

in struct Data {
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
    int pLightNum;
    int sLightNum;
    int dLightNum;
} data;
in vec3 pLightPos[10];


struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

uniform Material material;

vec3 applyAmbient(vec3 color) {
    return color * aLight.color;
}
*/
vec3 applyDirectedLight(vec3 color, vec3 normal, vec3 viewDir, int index) {
    /* vec3 lightDir = normalize(-dLights[i].direction);
        float diff = max(dot(normal, lightDir), 0.0);
        vec3 reflectedDir = reflect(-lightDir, normal);
        float spec = pow(max(dot(viewDir, reflectedDir), 0.0), material.shininess);
        vec3 diffuse = dLights[i].diffuse * diff * material.diffuse;
        vec3 specular = dLights[i].specular * spec * material.specular;
        return diffuse + specular;*/
}

vec3 applySourceLight(vec3 color, int index) { return vec3(0.0); }

vec3 applyPointLight(vec3 color, int index) { return vec3(0.0); }

void main()
{
    FragColor = vec4(0.0);
    material.diffuse = vec3(0.3);
    material.specular = vec3(0.3);
    material.ambient = vec3(0.3);
    data.dLightNum = 1;
    dLights[0].direction = vec3(1.0);
    dLights[0].color = vec3(1.0);
    dLights[0].specular = 0.3;
    dLights[0].diffuse = 0.3;
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm = normalize(texture(normalMap, data.TexCoord).rgb * 2.0 - 1.0);// god bless, everything is norm
    vec3 result = applyAmbient(color);
    for (int i = 0; i < data.sLights; i++) {
        resutl += applySourceLight(color, norm, viewDir, i);
    }
    //for (int i = 0; i < data.dLights; i++) {resutl += applySourceLight(color, i);}
    //for (int i = 0; i < data.pLights; i++) {resutl += applySourceLight(color, i);}
    FragColor = vec4(result, 1.0);
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm = texture(normalMap, data.TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    for (int i=0;i<data.pLightNum;i++){ //data.pLightNum
        // ambient
        vec3 ambient = pLights[i].ambinient * color;
        // diffuse
        vec3 lightDir = normalize(pLightPos[i] - data.TangentFragPos);
        float diff = max(dot(lightDir, norm), 0.0);
        vec3 diffuse = diff * color;
        // specular
        vec3 reflectDir = reflect(-lightDir, norm);
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(norm, halfwayDir), 0.0), 2.0);
        // spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0); //phong
        vec3 specular = vec3(0.3) * spec;// assuming bright white light color
        FragColor += vec4(ambient + diffuse + specular, 1.0);// vec4(norm,1.0);//textureColor*diff*vec4(lightColor, 1.0);
        //FragColor+=vec4(0.5*(lightDir+1.0),1.0);
    }

}
