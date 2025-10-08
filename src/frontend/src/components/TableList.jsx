import React from 'react'

export default function TableList({ tables, selected, onSelect, onRefresh }) {
  return (
    <div className="table-list">
      {tables.map(name => (
        <div
          key={name}  // ключ обязательно!
          onClick={() => onSelect(name)}
          style={{
            padding: '5px 10px',
            cursor: 'pointer',
            backgroundColor: selected === name ? '#007bff' : 'transparent',
            color: selected === name ? 'white' : 'black',
            marginBottom: '2px',
            borderRadius: '4px'
          }}
        >
          {name}
        </div>
      ))}
      <button onClick={onRefresh} style={{marginTop:'10px'}}>Refresh</button>
    </div>
  )
}