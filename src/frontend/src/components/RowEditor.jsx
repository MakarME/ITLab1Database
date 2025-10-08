import React, { useState, useEffect } from 'react'
import Modal from 'react-modal'
import api from '../utils/api'

Modal.setAppElement('#root')

export default function RowEditor({ isOpen, onClose, row, schema, index, tableName }){
  const [values, setValues] = useState([])

  useEffect(()=>{
    if(row) setValues(row.values.map(v=> valueToString(v)))
    else setValues(schema.map(()=>''))
  }, [row, schema, isOpen])

  async function onSave(){
    try{
      const parsed = schema.map((f,i)=> parseByType(values[i], f))
      if(index >=0) await api.updateRow(tableName, index, parsed)
      else await api.insertRow(tableName, parsed)
      onClose()
    }catch(err){ alert(err.message || 'Save failed') }
  }

  return (
    <Modal isOpen={isOpen} onRequestClose={onClose} className="modal" overlayClassName="overlay">
      <h3>{index>=0 ? 'Edit row':'Add row'}</h3>
      <div className="form-grid">
        {schema.map((f,i)=> (
          <div key={f.name} className="form-row">
            <label>{f.name} ({f.type})</label>
            <input value={values[i]||''} onChange={e=>{ const nv=[...values]; nv[i]=e.target.value; setValues(nv) }} />
          </div>
        ))}
      </div>
      <div className="modal-actions">
        <button onClick={onSave}>Save</button>
        <button onClick={onClose}>Cancel</button>
      </div>
    </Modal>
  )
}

function valueToString(v){ if(v==null) return ''; if(typeof v === 'object') return JSON.stringify(v); return String(v) }

function parseByType(s, field){ if(s==='' || s==null) return null;
  switch(field.type){
    case 'INTEGER': return parseInt(s);
    case 'REAL': return parseFloat(s.replace(',', '.'));
    case 'CHAR': if(s.length!==1) throw new Error('Char length must be 1'); return s.charAt(0);
    case 'STRING': return s;
    case 'COMPLEX_INTEGER': {
      // expected like "1+2i" or "1-2i"; simple parse
      const txt = s.trim(); const m = txt.match(/^([+-]?\d+)([+-]\d+)i$/i);
      if(!m) throw new Error('Bad complex integer format'); return { r: parseInt(m[1]), i: parseInt(m[2]) }
    }
    case 'COMPLEX_REAL': {
      const txt = s.trim().replace(',', '.'); const m = txt.match(/^([+-]?\d+(?:\.\d+)?)([+-]\d+(?:\.\d+)?)i$/i);
      if(!m) throw new Error('Bad complex real format'); return { r: parseFloat(m[1]), i: parseFloat(m[2]) }
    }
    default: return s
  }
}