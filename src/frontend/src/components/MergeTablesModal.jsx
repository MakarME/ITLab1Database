import React, { useState } from 'react'
import Modal from 'react-modal'
Modal.setAppElement('#root')

export default function MergeTablesModal({ isOpen, onRequestClose, tables, onMerge }){
  const [a,setA] = useState('')
  const [b,setB] = useState('')
  const [name,setName] = useState('')

  function submit(){ if(!a||!b||!name) return alert('Fill all'); onMerge({ a,b,newName:name }) }

  return (
    <Modal isOpen={isOpen} onRequestClose={onRequestClose} className="modal" overlayClassName="overlay">
      <h3>Merge tables</h3>
      <div className="form-row"><label>Table A</label>
        <select value={a} onChange={e=>setA(e.target.value)}>{["",...tables].map(t=> <option key={t} value={t}>{t}</option>)}</select>
      </div>
      <div className="form-row"><label>Table B</label>
        <select value={b} onChange={e=>setB(e.target.value)}>{["",...tables].map(t=> <option key={t} value={t}>{t}</option>)}</select>
      </div>
      <div className="form-row"><label>New name</label><input value={name} onChange={e=>setName(e.target.value)} /></div>
      <div className="modal-actions"><button onClick={submit}>Merge</button><button onClick={onRequestClose}>Cancel</button></div>
    </Modal>
  )
}