#version 300 es
precision mediump float;
out vec4 FragColor;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

in struct Data{
    vec3 normal;
    mat4 model2;
    vec3 FragPos;
    vec2 TexCoord;
    vec3 TangentLightPos;
    vec3 TangentViewPos;
    vec3 TangentFragPos;
} data;


void main()
{
    vec3 norm = texture(normalMap, data.TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 color =texture(textureSamp, data.TexCoord).rgb;
    // ambient
    vec3 ambient = 0.35 * color;
    // diffuse
    vec3 lightDir = normalize(data.TangentLightPos - data.TangentFragPos);
    float diff = max(dot(lightDir, norm), 0.0);
    vec3 diffuse = diff * color;
    // specular
    vec3 viewDir = normalize(data.TangentViewPos - data.TangentFragPos);


    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
    // spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
    vec3 specular = vec3(0.3) * spec;// assuming bright white light color
    FragColor = vec4(ambient + diffuse + specular, 1.0);// vec4(norm,1.0);//textureColor*diff*vec4(lightColor, 1.0);
}
