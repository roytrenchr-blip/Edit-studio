package com.editstudio.app.render

object ShaderLibrary {
    const val VERTEX_SHADER = """
        #version 300 es
        layout(location = 0) in vec4 aPosition;
        layout(location = 1) in vec2 aTexCoord;
        uniform mat4 uMvpMatrix;
        out vec2 vTexCoord;
        void main() {
            gl_Position = uMvpMatrix * aPosition;
            vTexCoord = aTexCoord;
        }
    """

    const val FRAGMENT_SHADER_COMPOSITE = """
        #version 300 es
        precision mediump float;
        in vec2 vTexCoord;
        uniform sampler2D uTexture;
        uniform float uOpacity;
        uniform float uRgbSplit;
        uniform float uGlitch;
        uniform float uVignette;
        uniform float uHueShift;
        uniform float uTime;
        out vec4 fragColor;

        vec3 rgb2hsv(vec3 c) {
            vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
            vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
            vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
            float d = q.x - min(q.w, q.y);
            float e = 1.0e-10;
            return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
        }

        vec3 hsv2rgb(vec3 c) {
            vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
            vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }

        void main() {
            vec2 uv = vTexCoord;
            if (uGlitch > 0.01) {
                float slice = floor(uv.y * 24.0);
                float shift = sin(slice * 12.9898 + uTime * 30.0) * uGlitch * 0.04;
                uv.x = fract(uv.x + shift);
            }
            vec4 base = texture(uTexture, uv);
            if (uRgbSplit > 0.001) {
                float r = texture(uTexture, uv + vec2(uRgbSplit * 0.02, 0.0)).r;
                float b = texture(uTexture, uv - vec2(uRgbSplit * 0.02, 0.0)).b;
                base.r = r;
                base.b = b;
            }
            if (abs(uHueShift) > 0.01) {
                vec3 hsv = rgb2hsv(base.rgb);
                hsv.x = fract(hsv.x + uHueShift);
                base.rgb = hsv2rgb(hsv);
            }
            if (uVignette > 0.01) {
                float dist = distance(vTexCoord, vec2(0.5));
                base.rgb *= smoothstep(0.95, 0.25, dist * (0.5 + uVignette * 0.7));
            }
            fragColor = vec4(base.rgb, base.a * uOpacity);
        }
    """
}
