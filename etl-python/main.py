import os
import mysql.connector
from flask import Flask, request, jsonify
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

# DB Configurations from .env
SOURCE_DB_CONFIG = {
    'host': os.getenv('SOURCE_DB_HOST'),
    'user': os.getenv('SOURCE_DB_USER'),
    'password': os.getenv('SOURCE_DB_PASS'),
    'database': os.getenv('SOURCE_DB_NAME'),
    'port': int(os.getenv('SOURCE_DB_PORT', 3307))
}

TARGET_DB_CONFIG = {
    'host': os.getenv('TARGET_DB_HOST'),
    'user': os.getenv('TARGET_DB_USER'),
    'password': os.getenv('TARGET_DB_PASS'),
    'database': os.getenv('TARGET_DB_NAME'),
    'port': int(os.getenv('TARGET_DB_PORT', 3306))
}

def connect_db(config):
    return mysql.connector.connect(**config)

def transform_data(row):
    """
    Example transformation: Mapping fields, type conversion, etc.
    """
    # Assuming row is a dictionary or tuple from Source
    transformed = list(row)
    # Perform transformations here
    return transformed

@app.route('/etl/run', methods=['GET'])
def run_etl():
    mode = request.args.get('mode', 'batch')
    print(f"Starting ETL in {mode} mode...")
    
    columns = [
        'insp_inst_cd', 'doc_no', 'doc_cycl', 'insp_inst_nm', 'ntfctn_rtrcn_yn',
        'rpt_type_cd', 'rpt_type_nm', 'prdct_nm', 'prdct_type_nm', 'plor_nm',
        'barcd_no', 'mnftr_ymd', 'rtl_term_cn', 'pac_unit_cn', 'safe_cert_no',
        'prdct_prmsn_no', 'mnftr_no', 'rtrvl_rsn_cd', 'rtrvl_rsn_nm', 'rpt_inst_nm',
        'rpt_pic_nm', 'rpt_pic_telno', 'insp_se_nm', 'clt_inst_nm', 'clt_ymd',
        'rmrk_cn', 'rpt_ymd', 'ntfctn_dt', 'cmd_bgng_dd_cn', 'cert_nm',
        'cert_cd_cn', 'std_gds_clsf_cd', 'created_at'
    ]
    col_str = ", ".join(columns)
    placeholders = ", ".join(["%s"] * (len(columns) + 1)) # +1 for save_time

    try:
        source_conn = connect_db(SOURCE_DB_CONFIG)
        target_conn = connect_db(TARGET_DB_CONFIG)
        
        source_cursor = source_conn.cursor(dictionary=True)
        target_cursor = target_conn.cursor()

        migrated_count = 0
        
        if mode == 'batch':
            # Batch Mode: Full migration
            source_cursor.execute(f"SELECT {col_str} FROM unsafe_product")
            rows = source_cursor.fetchall()
            
            for row in rows:
                query = f"""
                    INSERT INTO eojin_project_unsafe_product ({col_str}, save_time)
                    VALUES ({placeholders})
                """
                values = [row[col] for col in columns]
                values.append(datetime.now())
                target_cursor.execute(query, values)
                migrated_count += 1
                
        elif mode == 'realtime':
            # Real-time Mode: Using the newly added created_at timestamp
            last_sync_time = request.args.get('last_sync_time', '1970-01-01 00:00:00')
            
            source_cursor.execute(f"SELECT {col_str} FROM unsafe_product WHERE created_at > %s", (last_sync_time,))
            rows = source_cursor.fetchall()
            
            for row in rows:
                update_parts = [f"{col}=VALUES({col})" for col in columns if col != 'doc_no']
                update_parts.append("save_time=VALUES(save_time)")
                update_str = ", ".join(update_parts)

                query = f"""
                    INSERT INTO eojin_project_unsafe_product ({col_str}, save_time)
                    VALUES ({placeholders})
                    ON DUPLICATE KEY UPDATE {update_str}
                """
                values = [row[col] for col in columns]
                values.append(datetime.now())
                target_cursor.execute(query, values)
                migrated_count += 1

        target_conn.commit()
        
        source_cursor.close()
        target_cursor.close()
        source_conn.close()
        target_conn.close()

        return jsonify({
            'status': 'SUCCESS',
            'count': migrated_count,
            'mode': mode
        })

    except Exception as e:
        print(f"Error during ETL: {str(e)}")
        return jsonify({
            'status': 'FAIL',
            'error': str(e)
        }), 500

if __name__ == '__main__':
    # Use placeholder port
    app.run(host='0.0.0.0', port=int(os.getenv('PORT', 5000)))
