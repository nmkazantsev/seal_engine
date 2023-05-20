#version 300 es
precision mediump float;
out vec4 FragColor;
in vec2 TexCoord;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

in vec3 normal;
in mat4 model2;
in vec3 FragPos;
in vec3 TangentLightPos;
in vec3 TangentViewPos;
in vec3 TangentFragPos;


void main()
{
    vec3 norm = texture(normalMap, TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 color = texture(textureSamp, TexCoord).rgb;
    // ambient
    vec3 ambient = 0.15 * color;
    // diffuse
    vec3 lightDir = normalize(TangentLightPos - TangentFragPos);
    float diff = max(dot(lightDir, norm), 0.0);
    vec3 diffuse = diff * color;
    // specular
    vec3 viewDir = normalize(TangentViewPos - TangentFragPos);


    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
   // spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
    vec3 specular = vec3(0.3) * spec;// assuming bright white light color
    FragColor = vec4(ambient + diffuse + specular, 1.0);// vec4(norm,1.0);//textureColor*diff*vec4(lightColor, 1.0);
}
