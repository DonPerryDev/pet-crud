import type { ReactNode } from 'react'
import { useEffect } from 'react'
import { createPortal } from 'react-dom'

interface ConfirmDeleteModalProps {
  title: string
  description: string
  confirmLabel: string
  cancelLabel: string
  isDeleting: boolean
  onConfirm: () => void
  onCancel: () => void
}

export function ConfirmDeleteModal({
  title,
  description,
  confirmLabel,
  cancelLabel,
  isDeleting,
  onConfirm,
  onCancel,
}: ConfirmDeleteModalProps): ReactNode {
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent): void {
      if (e.key === 'Escape') onCancel()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [onCancel])

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-delete-title"
    >
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onCancel}
      />

      {/* Modal */}
      <div className="relative z-10 w-full max-w-sm rounded-2xl bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 shadow-xl p-6 flex flex-col gap-5">
        {/* Icon + title */}
        <div className="flex flex-col items-center gap-3 text-center">
          <div className="flex items-center justify-center size-14 rounded-full bg-red-100 dark:bg-red-900/30">
            <span className="material-symbols-outlined text-2xl text-red-500">delete</span>
          </div>
          <h2 id="confirm-delete-title" className="text-sage-dark dark:text-white text-lg font-bold">
            {title}
          </h2>
          <p className="text-sage-muted text-sm leading-relaxed">{description}</p>
        </div>

        {/* Actions */}
        <div className="flex gap-3">
          <button
            type="button"
            onClick={onCancel}
            disabled={isDeleting}
            className="flex-1 h-11 rounded-full border border-gray-200 dark:border-gray-700 text-sage-dark dark:text-white text-sm font-bold hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors disabled:opacity-60"
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isDeleting}
            className="flex-1 h-11 rounded-full bg-red-500 text-white text-sm font-bold hover:bg-red-600 transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {isDeleting ? '...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  )
}
