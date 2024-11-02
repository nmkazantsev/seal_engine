#version 300 es
precision mediump float;
uniform vec3 vColor;
out vec4 FragColor;
void main() {
     FragColor = vec4(vColor, 1.0);
 }