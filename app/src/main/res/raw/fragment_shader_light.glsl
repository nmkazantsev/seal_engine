#version 300 es
precision mediump float;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

struct PointLight{
    vec3 lightPos;
    float specular;
    float diffuse;
    float ambinient;
};

uniform PointLight pLights [10];

in struct Data{
    mat4 model2;
    vec3 normal;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
    int pLightNum;
} data;
in vec3 pLightPos[10];


void main()
{
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);
    vec3 norm = texture(normalMap, data.TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 color = texture(textureSamp, data.TexCoord).rgb;
    for (int i=0;i<1;i++){//data.pLightNum
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
