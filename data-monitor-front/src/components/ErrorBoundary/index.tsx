import { Component } from 'react';
import type { ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

/**
 * 全局错误边界，防止单个组件崩溃导致白屏
 *
 * @author zhoulu
 * @since 2026-04-23
 */
export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            width: '100vw',
            height: '100vh',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'radial-gradient(ellipse at 50% 30%, #0a1e4a 0%, #030b1f 60%, #010510 100%)',
            color: '#8798af',
            fontFamily: 'ui-monospace, monospace',
          }}
        >
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>⚠️</div>
          <div style={{ fontSize: '18px', color: '#00f3ff', marginBottom: '8px' }}>
            页面加载异常
          </div>
          <div style={{ fontSize: '13px', maxWidth: '400px', textAlign: 'center', lineHeight: 1.6 }}>
            {this.state.error?.message ?? '未知错误'}
          </div>
          <button
            onClick={() => {
              this.setState({ hasError: false, error: null });
              window.location.reload();
            }}
            style={{
              marginTop: '24px',
              padding: '8px 24px',
              background: 'rgba(0, 243, 255, 0.1)',
              border: '1px solid rgba(0, 243, 255, 0.3)',
              color: '#00f3ff',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            重新加载
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
