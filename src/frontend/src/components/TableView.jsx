import React, { useState } from 'react'
import RowEditor from './RowEditor'
import CSVTools from '../utils/csv'
import api from '../utils/api'

export default function TableView({ name, rows, schema, refresh }){
  const [edit, setEdit] = useState({open:false, row:null, index:-1})

  if(!name) return <div className="empty">No table selected</div>

  const onAdd = ()=> setEdit({ open:true, row: null, index: -1 })
  const onEdit = (r,i)=> setEdit({ open:true, row: r, index: i })
  const onDelete = async (i)=>{ if(!confirm('Delete row?')) return; await api.deleteRow(name,i); refresh() }

  const onImport = async (file)=>{
    const data = new FormData(); data.append('file', file)
    await api.importCsv(name, data)
    refresh()
  }

  const onExport = async ()=>{
    const data = await api.exportCsv(name)
    const blob = new Blob([data], { type:'text/csv' })
    const url = URL.createObjectURL(blob); const a=document.createElement('a'); a.href=url; a.download = name + '.csv'; a.click(); URL.revokeObjectURL(url)
  }

  return (
    <div className="table-view">
      <div className="tv-header">Table: {name}</div>
      <div className="tv-actions">
        <button onClick={onAdd}>Add row</button>
        <button onClick={onExport}>Export CSV</button>
        <label className="file-btn">Import CSV<input type="file" accept=".csv" onChange={e=>onImport(e.target.files[0])} /></label>
      </div>

      <table className="data-table">
        <thead>
          <tr>{schema.map(s=> <th key={s.name}>{s.name}</th>)}<th>Actions</th></tr>
        </thead>
        <tbody>
          {rows.map((r,idx)=> (
            <tr key={idx}>
              {schema.map((s,i)=> <td key={i}>{renderValue(r.values[i])}</td>)}
              <td>
                <button onClick={()=>onEdit(r, idx)}>Edit</button>
                <button onClick={()=>onDelete(idx)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <RowEditor isOpen={edit.open} row={edit.row} schema={schema} index={edit.index} tableName={name} onClose={()=>{ setEdit({open:false}) ; refresh() }} />
    </div>
  )
}

function renderValue(v){
  if(v === null || v === undefined) return ''
  if(typeof v === 'object') return JSON.stringify(v)
  return String(v)
}