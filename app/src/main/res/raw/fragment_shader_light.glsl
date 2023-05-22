#version 300 es
precision mediump float;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

struct DirectionalLight{
    vec3 lightPos;
    float specular;
    float diffuse;
    float ambinient;
};

uniform DirectionalLight dirLights [10];

in struct Data{
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
    int dirLightNum;
} data;
in vec3 lightPos[10];


void main()
{
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm = texture(normalMap, data.TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    for (int i=0;i<data.dirLightNum;i++){
        // ambient
        vec3 ambient = dirLights[i].ambinient * color;
        // diffuse
        vec3 lightDir = normalize(lightPos[i] - data.TangentFragPos);
        float diff = max(dot(lightDir, norm), 0.0);
        vec3 diffuse = diff * color;
        // specular
        vec3 reflectDir = reflect(-lightDir, norm);
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
        // spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0); //phong
        vec3 specular = vec3(0.3) * spec;// assuming bright white light color
        FragColor += vec4(ambient + diffuse + specular, 1.0);// vec4(norm,1.0);//textureColor*diff*vec4(lightColor, 1.0);
    }
}
