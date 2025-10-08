const BASE = '/api'

async function request(url, options = {}) {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || res.statusText)
  }
  if(res.status !== 204) return res.json().catch(()=>null)
  return null
}

export async function listTables() {
  return request(`${BASE}/tables`)
}

export async function createTable({name, schema}) {
  return request(`${BASE}/tables`, { method: 'POST', body: JSON.stringify({name,schema}) })
}

export async function dropTable(name) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}`, { method: 'DELETE' })
}

export async function getRows(name) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}/rows`)
}

export async function getSchema(name) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}/schema`)
}

export async function insertRow(name, values) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}/rows`, { method: 'POST', body: JSON.stringify(values) })
}

export async function updateRow(name, index, values) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}/rows/${index}`, { method: 'PUT', body: JSON.stringify(values) })
}

export async function deleteRow(name, index) {
  return request(`${BASE}/tables/${encodeURIComponent(name)}/rows/${index}`, { method: 'DELETE' })
}

export async function mergeTables({a,b,newName}) {
  return request(`${BASE}/tables/merge`, { method: 'POST', body: JSON.stringify({a,b,newName}) })
}

export async function importCsv(tableName, file) {
  const form = new FormData()
  form.append('file', file)
  const res = await fetch(`${BASE}/tables/${encodeURIComponent(tableName)}/importCsv`, { method: 'POST', body: form })
  if(!res.ok) throw new Error(await res.text())
}

export async function exportCsv(tableName) {
  const res = await fetch(`${BASE}/tables/${encodeURIComponent(tableName)}/exportCsv`)
  if(!res.ok) throw new Error(await res.text())
  return res.blob()
}

// Optional: backend endpoints for DB save/load (if implemented)
export async function exportDatabase() {
  return request(`${BASE}/db/export`)
}

export async function loadDatabase(dump) {
  return request(`${BASE}/db/load`, { method:'POST', body: JSON.stringify(dump) })
}

export default {
  listTables, createTable, dropTable,
  getRows, getSchema, insertRow, updateRow, deleteRow,
  mergeTables, importCsv, exportCsv,
  exportDatabase, loadDatabase
}