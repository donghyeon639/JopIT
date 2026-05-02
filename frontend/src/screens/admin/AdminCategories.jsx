import React, { useEffect, useState } from "react";
import AdminLayout from "../../components/admin/AdminLayout.jsx";
import { adminQuestionCategories } from "../../api/adminApi.js";

export default function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [newCatName, setNewCatName] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving]   = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editName, setEditName] = useState("");

  const loadCategories = () =>
    adminQuestionCategories.list().then(setCategories).finally(() => setLoading(false));

  useEffect(() => { loadCategories(); }, []);

  const addCategory = async () => {
    if (!newCatName.trim()) return;
    setSaving(true);
    try {
      const created = await adminQuestionCategories.create({ name: newCatName.trim() });
      setCategories(prev => [...prev, created]);
      setNewCatName("");
    } catch (e) { alert(e.message); }
    finally { setSaving(false); }
  };

  const startEdit = (cat) => {
    setEditingId(cat.id);
    setEditName(cat.name);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditName("");
  };

  const saveEdit = async (id) => {
    const name = editName.trim();
    if (!name) return;
    try {
      const updated = await adminQuestionCategories.update(id, { name });
      setCategories(prev => prev.map(c => c.id === id ? updated : c));
      cancelEdit();
    } catch (e) {
      alert(e.message);
    }
  };

  const deleteCategory = async (id, name) => {
    if (!window.confirm(`"${name}" 카테고리를 삭제하시겠습니까?\n해당 카테고리의 문제는 모두 영향을 받을 수 있습니다.`)) return;
    try {
      await adminQuestionCategories.remove(id);
      setCategories(prev => prev.filter(c => c.id !== id));
    } catch (e) {
      alert(e.message);
    }
  };

  return (
    <AdminLayout>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 6 }}>질문 카테고리 관리</h1>
      <p style={{ color: "var(--gray-500)", marginBottom: 28 }}>
        CS 주제별 카테고리를 관리합니다. (예: 운영체제, 네트워크, 데이터베이스, 자료구조, 백엔드, 프론트엔드, 클라우드)
      </p>

      {/* 카테고리 추가 */}
      <div style={{ background: "#fff", borderRadius: 12, border: "1px solid var(--gray-200)", padding: "16px 18px", marginBottom: 20 }}>
        <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 10 }}>새 카테고리 추가</div>
        <div style={{ display: "flex", gap: 10 }}>
          <input
            type="text"
            value={newCatName}
            onChange={e => setNewCatName(e.target.value)}
            onKeyDown={e => e.key === "Enter" && addCategory()}
            placeholder="카테고리명 (예: 운영체제, 네트워크)"
            style={inputStyle}
          />
          <button className="btn btn-primary btn-sm" onClick={addCategory} disabled={saving || !newCatName.trim()}>
            추가
          </button>
        </div>
      </div>

      {/* 카테고리 목록 */}
      {loading ? (
        <div style={{ padding: 40, textAlign: "center", color: "var(--gray-400)" }}>불러오는 중...</div>
      ) : categories.length === 0 ? (
        <div style={{ padding: 40, textAlign: "center", color: "var(--gray-400)" }}>등록된 카테고리가 없습니다.</div>
      ) : (
        <div style={{ background: "#fff", borderRadius: 12, border: "1px solid var(--gray-200)", overflow: "hidden" }}>
          {categories.map((cat, idx) => {
            const isEditing = editingId === cat.id;
            return (
              <div
                key={cat.id}
                style={{
                  display: "flex", alignItems: "center", padding: "14px 18px", gap: 10,
                  borderBottom: idx < categories.length - 1 ? "1px solid var(--gray-100)" : "none",
                }}
              >
                {isEditing ? (
                  <>
                    <input
                      type="text"
                      value={editName}
                      onChange={e => setEditName(e.target.value)}
                      onKeyDown={e => {
                        if (e.key === "Enter") saveEdit(cat.id);
                        if (e.key === "Escape") cancelEdit();
                      }}
                      autoFocus
                      style={{ ...inputStyle, flex: 1 }}
                    />
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={() => saveEdit(cat.id)}
                      disabled={!editName.trim()}
                    >
                      저장
                    </button>
                    <button className="btn btn-ghost btn-sm" onClick={cancelEdit}>
                      취소
                    </button>
                  </>
                ) : (
                  <>
                    <span style={{ fontSize: 14, fontWeight: 600, flex: 1 }}>{cat.name}</span>
                    <button
                      onClick={() => startEdit(cat)}
                      style={{ background: "none", border: "none", color: "var(--blue-600)", cursor: "pointer", fontSize: 12, padding: "4px 8px" }}
                    >
                      수정
                    </button>
                    <button
                      onClick={() => deleteCategory(cat.id, cat.name)}
                      style={{ background: "none", border: "none", color: "#EF4444", cursor: "pointer", fontSize: 12, padding: "4px 8px" }}
                    >
                      삭제
                    </button>
                  </>
                )}
              </div>
            );
          })}
        </div>
      )}
    </AdminLayout>
  );
}

const inputStyle = {
  flex: 1,
  padding: "9px 12px",
  borderRadius: 8,
  border: "1px solid var(--gray-200)",
  fontSize: 14,
  outline: "none",
};
