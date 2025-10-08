import React, { useEffect, useState } from 'react'
import TableList from './components/TableList'
import TableView from './components/TableView'
import CreateTableModal from './components/CreateTableModal'
import MergeTablesModal from './components/MergeTablesModal'
import { ToastContainer, toast } from 'react-toastify'
import api from './utils/api'

export default function App() {
  const [tables, setTables] = useState([])
  const [selected, setSelected] = useState(null)
  const [rows, setRows] = useState([])
  const [schema, setSchema] = useState([])
  const [createOpen, setCreateOpen] = useState(false)
  const [mergeOpen, setMergeOpen] = useState(false)

  useEffect(()=>{ fetchTables() }, [])

  async function fetchTables(){
    try{
      const t = await api.listTables()
      setTables(t)
      if(t.length && !selected) setSelected(t[0])
    } catch(err){ toast.error(err.message || 'Cannot list tables') }
  }

  useEffect(()=>{ if(selected) loadTable(selected) }, [selected])

  async function loadTable(name){
    try{
      const [r,s] = await Promise.all([api.getRows(name), api.getSchema(name)])
      setRows(r)
      setSchema(s)
    }catch(err){ toast.error(err.message || 'Cannot load table') }
  }

  const onCreate = async (req) => {
    try{ await api.createTable(req); toast.success('Created'); setCreateOpen(false); fetchTables() }
    catch(err){ toast.error(err.message || 'Create failed') }
  }

  const onDrop = async (name) => {
    if(!confirm(`Drop table '${name}'?`)) return
    try{ await api.dropTable(name); toast.success('Dropped'); fetchTables(); setSelected(null) }
    catch(err){ toast.error(err.message || 'Drop failed') }
  }

  const onMerge = async (req) => {
    try{ await api.mergeTables(req); toast.success('Merged'); setMergeOpen(false); fetchTables() }
    catch(err){ toast.error(err.message || 'Merge failed') }
  }

  const onExportDB = async () => {
    try{
      const dump = await api.exportDatabase()
      const blob = new Blob([JSON.stringify(dump, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url; a.download = 'database.json'; a.click(); URL.revokeObjectURL(url)
    } catch(err){ toast.error(err.message || 'Export failed') }
  }

  const onLoadDB = async () => {
    const input = document.createElement('input'); input.type='file'
    input.onchange = async (e)=>{
      const f = e.target.files[0]; if(!f) return
      try{
        const text = await f.text()
        const dump = JSON.parse(text)
        await api.loadDatabase(dump)
        await fetchTables()
        toast.success('Database loaded')
      } catch(err){ toast.error('Load failed: '+(err.message||err)) }
    }
    input.click()
  }

  return (
    <div className="app-root">
      <ToastContainer position="top-right" />
      <div className="left-pane">
        <div className="left-header">Tables</div>
        <TableList
          tables={tables}
          selected={selected}
          onSelect={setSelected}
          onRefresh={fetchTables}
        />
        <div className="left-buttons">
          <button onClick={()=>setCreateOpen(true)}>Create table</button>
          <button onClick={()=>selected && onDrop(selected)}>Drop table</button>
          <button onClick={onExportDB}>Export DB</button>
          <button onClick={onLoadDB}>Load DB</button>
          <button onClick={()=>setMergeOpen(true)}>Merge tables</button>
        </div>
      </div>
      <div className="center-pane">
        <TableView
          name={selected}
          rows={rows}
          schema={schema}
          refresh={()=>loadTable(selected)}
        />
      </div>

      <CreateTableModal isOpen={createOpen} onRequestClose={()=>setCreateOpen(false)} onCreate={onCreate} />
      <MergeTablesModal isOpen={mergeOpen} onRequestClose={()=>setMergeOpen(false)} tables={tables} onMerge={onMerge} />
    </div>
  )
}
