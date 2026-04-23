import { Component as ReactComponent } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { componentRegistry } from './A2uiRegistry';
import type { ChatMessageChunk } from '../../types/ai-chat';

interface Props {
  chunks: ChatMessageChunk[];
}

interface ErrorBoundaryProps {
  componentName: string;
  children: React.ReactNode;
}

interface ErrorBoundaryState {
  hasError: boolean;
}

/**
 * 组件渲染错误边界
 * 捕获 A2UI 组件渲染过程中的异常，显示降级占位符
 */
class ComponentErrorBoundary extends ReactComponent<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: unknown, errorInfo: React.ErrorInfo): void {
    console.error(
      `组件渲染异常 (${this.props.componentName}):`,
      error,
      errorInfo,
    );
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            color: '#ff6b4a',
            fontSize: '12px',
            padding: '10px 12px',
            background: 'rgba(255,100,100,0.08)',
            border: '1px solid rgba(255,100,100,0.15)',
            borderRadius: '4px',
            lineHeight: 1.5,
          }}
        >
          <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
            组件渲染异常
          </div>
          <div style={{ color: '#8798af', fontSize: '11px' }}>
            组件名：{this.props.componentName}
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

export default function DynamicMessageRenderer({ chunks }: Props) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {chunks.map((chunk, idx) => {
        if (chunk.type === 'text') {
          return (
            <div
              key={idx}
              style={{
                color: '#e0e6f0',
                lineHeight: 1.7,
                fontSize: '14px',
              }}
            >
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {chunk.content}
              </ReactMarkdown>
            </div>
          );
        }

        const Component = componentRegistry[chunk.componentName];
        if (!Component) {
          return (
            <div
              key={idx}
              style={{
                color: '#ff6b4a',
                fontSize: '12px',
                padding: '10px 12px',
                background: 'rgba(255,100,100,0.08)',
                border: '1px solid rgba(255,100,100,0.15)',
                borderRadius: '4px',
                lineHeight: 1.5,
              }}
            >
              <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                组件加载失败
              </div>
              <div style={{ color: '#8798af', fontSize: '11px' }}>
                组件名：{chunk.componentName}
              </div>
            </div>
          );
        }

        return (
          <ComponentErrorBoundary key={idx} componentName={chunk.componentName}>
            <div
              style={{
                background: 'rgba(0,243,255,0.03)',
                border: '1px solid rgba(0,243,255,0.1)',
                borderRadius: '8px',
                padding: '12px',
              }}
            >
              <Component name={chunk.componentName} {...chunk.props} />
            </div>
          </ComponentErrorBoundary>
        );
      })}
    </div>
  );
}
