#version 300 es
precision mediump float;
layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec4 gAlbedoSpec;

in vec3 FragPos;
in vec2 TexCoord;
in vec3 normal;
in vec3 aTv;
in vec3 aBv;
uniform int normalMapEnable;
uniform sampler2D textureSamp;
uniform sampler2D normalMap;

void main()
{
   vec3 norm=normalize(normal);
    if(normalMapEnable==0){
        gNormal = norm;
    }else{
        vec3 n = normalize(texture(normalMap, vec2(TexCoord.x,TexCoord.y)).rgb * 2.0 - 1.0);
        vec3 aT=aTv;
        vec3 aB=aBv;
        gNormal = vec3((aT*n.x + aB*n.y + norm*n.z));
    }
    gPosition=FragPos.xyz;
    gAlbedoSpec = texture(textureSamp, TexCoord);
}