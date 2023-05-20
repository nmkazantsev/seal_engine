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
//vec3 TangentViewPos;
in vec3 TangentFragPos;



//vec3 directionalLight = normalize(vec3(1.0, 1.0, 0.0));// directional light
vec3 pointLight = vec3(0.0, -2.0, 2.0);
vec3 lightColor = vec3(1.0, 1.0, 1.0);

void main()
{
    float bright = min(pow(1.0/length(pointLight - FragPos), 1.4), 1.0);
    // vec3 direction = normalize(pointLight - FragPos);
    //vec3 norm = mat3(transpose(inverse(model2))) * normal;
    vec3 norm = texture(normalMap, TexCoord).rgb;
    norm = normalize(norm * 2.0 - 1.0);
    vec3 direction=normalize(TangentLightPos - TangentFragPos);

    float diff = max(dot(norm, direction), 0.0)*bright+0.2;
    vec4 textureColor = vec4(vec3(1.0),1.0);//texture(textureSamp, TexCoord);
    FragColor = textureColor*diff*vec4(lightColor, 1.0);// vec4(norm,1.0);//textureColor*diff*vec4(lightColor, 1.0);
}
