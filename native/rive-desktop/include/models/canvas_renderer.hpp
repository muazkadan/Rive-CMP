#pragma once

#include <rive/renderer.hpp>
#include "helpers/rive_log.hpp"
#include "include/core/SkCanvas.h"
#include "include/core/SkImageInfo.h"
#include "include/core/SkImage.h"

class CanvasRenderer : public rive::Renderer {
protected:
    std::unique_ptr<SkCanvas> m_Canvas;
    std::vector<float> m_opacityStack{1.0f};
    int m_width = -1;
    int m_height = -1;

public:
    CanvasRenderer() = default;

    [[nodiscard]] int width() const { return m_width; }
    [[nodiscard]] int height() const { return m_height; }
    [[nodiscard]] bool isBound() const { return m_Canvas != nullptr; }


    void bind(void *pixels, int width, int height) {
        SkImageInfo info = SkImageInfo::MakeN32Premul(width, height);
        m_Canvas = SkCanvas::MakeRasterDirect(info, pixels, width * 4);
        if (!m_Canvas)
            rive_desktop::RiveLogE("RiveN/CanvasRenderer",
                                   "MakeRasterDirect failed (%dx%d)",
                                   width,
                                   height);
        m_width = width;
        m_height = height;
    }

    void unbind() {
        m_Canvas.reset();
    }

    void save() override;

    void restore() override;

    void transform(const rive::Mat2D &transform) override;

    void modulateOpacity(float opacity) override;

    void drawPath(rive::RenderPath *path, rive::RenderPaint *paint) override;

    void clipPath(rive::RenderPath *path) override;

    void drawImage(const rive::RenderImage *, rive::ImageSampler, rive::BlendMode, float opacity) override;

    void drawImageMesh(const rive::RenderImage *, rive::ImageSampler, rive::rcp<rive::RenderBuffer> vertices_f32,
                       rive::rcp<rive::RenderBuffer> uvCoords_f32, rive::rcp<rive::RenderBuffer> indices_u16,
                       uint32_t vertexCount, uint32_t indexCount, rive::BlendMode, float opacity) override;
};
