import React, { useState } from 'react'
import Modal from 'react-modal'
Modal.setAppElement('#root')

const TYPES = ['INTEGER','REAL','CHAR','STRING','COMPLEX_INTEGER','COMPLEX_REAL']

export default function CreateTableModal({ isOpen, onRequestClose, onCreate }){
  const [name, setName] = useState('')
  const [fields, setFields] = useState([{ name:'id', type:'INTEGER', nullable:false, size: null }])

  function addField(){ setFields([...fields, { name:'', type:'STRING', nullable:true, size:null }]) }
  function updateField(i, patch){ const f=[...fields]; f[i] = {...f[i], ...patch}; setFields(f) }
  function removeField(i){ const f=[...fields]; f.splice(i,1); setFields(f) }

  function submit(){
    if(!name.trim()) return alert('Name required')
    if(fields.length===0) return alert('Add fields')
    onCreate({ name, schema: fields.map(f=> ({ name: f.name, type: f.type, nullable: !!f.nullable, size: f.size })) })
  }

  return (
    <Modal isOpen={isOpen} onRequestClose={onRequestClose} className="modal" overlayClassName="overlay">
      <h3>Create table</h3>
      <div className="form-row"><label>Table name</label><input value={name} onChange={e=>setName(e.target.value)} /></div>
      <div className="fields-list">
        {fields.map((f,i)=> (
          <div key={i} className="field-row">
            <input placeholder="name" value={f.name} onChange={e=>updateField(i,{name:e.target.value})} />
            <select value={f.type} onChange={e=>updateField(i,{type:e.target.value})}>{TYPES.map(t=> <option key={t}>{t}</option>)}</select>
            <label><input type="checkbox" checked={f.nullable} onChange={e=>updateField(i,{nullable:e.target.checked})} />nullable</label>
            <button onClick={()=>removeField(i)}>Remove</button>
          </div>
        ))}
      </div>
      <div className="modal-actions">
        <button onClick={addField}>Add field</button>
        <button onClick={submit}>Create</button>
        <button onClick={onRequestClose}>Cancel</button>
      </div>
    </Modal>
  )
}