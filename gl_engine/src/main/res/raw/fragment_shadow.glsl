#version 300 es
precision mediump float;
out vec4 FragColor;
void main()
{
    FragColor = vec4(vec3(gl_FragCoord.z),  0.0);
    // gl_FragDepth = gl_FragCoord.z;
}