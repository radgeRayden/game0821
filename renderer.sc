using import struct
using import glm
using import Map

let window = (import .bottle.src.window)
let wgpu = (import .bottle.src.FFI.wgpu)

using import .spritebatch

inline &local (T ...)
    &
        local T
            ...

struct GfxState plain
    surface : wgpu.Surface
    adapter : wgpu.Adapter
    device  : wgpu.Device
    swapchain : wgpu.SwapChain
    queue : wgpu.Queue



global istate : GfxState

fn create-wgpu-surface ()
    from window let get-native-window-info
    static-match operating-system
    case 'linux
        let x11-display x11-window = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromXlib
                            chain =
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromXlib
                            display = (x11-display as voidstar)
                            window = (x11-window as u32)
                        mutable@ wgpu.ChainedStruct
    case 'windows
        let hinstance hwnd = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromWindowsHWND
                            chain =
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromWindowsHWND
                            hinstance = hinstance
                            hwnd = hwnd
                        mutable@ wgpu.ChainedStruct
    default
        error "OS not supported"

fn update-swapchain ()
    let ww wh = (window.size)
    istate.swapchain =
        wgpu.DeviceCreateSwapChain istate.device istate.surface
            &local wgpu.SwapChainDescriptor
                label = "swapchain"
                usage = wgpu.TextureUsage.RenderAttachment
                format = wgpu.TextureFormat.BGRA8UnormSrgb
                width = (ww as u32)
                height = (wh as u32)
                presentMode = wgpu.PresentMode.Fifo

fn init ()
    istate.surface = (create-wgpu-surface)
    wgpu.InstanceRequestAdapter null
        &local wgpu.RequestAdapterOptions
            compatibleSurface = istate.surface
        fn (result userdata)
            istate.adapter = result
        null
    wgpu.AdapterRequestDevice istate.adapter
        &local wgpu.DeviceDescriptor
        fn (result userdata)
            istate.device = result
        null

    update-swapchain;
    istate.queue = (wgpu.DeviceGetQueue istate.device)

fn... present (draw-fn : (pointer (function void wgpu.RenderPassEncoder)))
    let swapchain-image = (wgpu.SwapChainGetCurrentTextureView istate.swapchain)
    if (swapchain-image == null)
        update-swapchain;
        return;

    let cmd-encoder =
        wgpu.DeviceCreateCommandEncoder istate.device
            &local wgpu.CommandEncoderDescriptor
                label = "command encoder"

    let rp =
        wgpu.CommandEncoderBeginRenderPass cmd-encoder
            &local wgpu.RenderPassDescriptor
                label = "output render pass"
                colorAttachmentCount = 1
                colorAttachments =
                    &local wgpu.RenderPassColorAttachmentDescriptor
                        attachment = swapchain-image
                        clearColor = (typeinit 0.017 0.017 0.017 1.0)

    draw-fn rp

    wgpu.RenderPassEncoderEndPass rp

    local cmd-buffer =
        wgpu.CommandEncoderFinish cmd-encoder
            &local wgpu.CommandBufferDescriptor
                label = "command buffer"

    wgpu.QueueSubmit istate.queue 1 &cmd-buffer
    wgpu.SwapChainPresent istate.swapchain
    ;

global layers : (Map string SpriteBatch)

fn new-layer (name atlas)
    'set layers name (SpriteBatch atlas)

fn get-layer (name)
    try
        'get layers name
    else
        assert false (.. "No layer named " (tostring name) " found.")
do
    let
        init
        present
        new-layer
        get-layer
    locals;
