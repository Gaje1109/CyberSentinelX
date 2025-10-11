import requests
import os
import time
from .models import Link
from . import scanner

## 2nd Validation
def run_secondary_validation(session, url):
    print(f"Checking '{url}' with secondary validation...")
    SECOND_KEY = os.getenv('SECOND_KEY')
    SECOND_URL = os.getenv('SECOND_VALIDATION_API_URL')
    api_url = f'{SECOND_URL}{SECOND_KEY}'

    payload = {'client': {'clientId': 'cybersentinelx-linkscanner'},
               'threatInfo': {'threatTypes': ['MALWARE', 'SOCIAL_ENGINEERING'], 'platformTypes': ['ANY_PLATFORM'],
                              'threatEntryTypes': ['URL'], 'threatEntries': [{'url': url}]}}
    try:
        response = session.post(api_url, json=payload, timeout=10)
        response.raise_for_status()
        return 'Unsafe' if response.json() else 'Safe'
    except requests.exceptions.RequestException as e:
        print(f"ERROR: Secondary check failed for {url}: {e}")
        return 'API Error'


## Third validation
def run_tertiary_validation(session, url):
    """
    Level 3 Validation: Deep-dive analysis with an aggregator service.
    (This is a replacement for VirusTotal).
    """
    THREE_KEY = os.getenv('THREE_KEY')
    THIRD_URL = os.getenv('THIRD_VALIDATION_API_URL')
    if not THREE_KEY:
        print("ERROR: Tertiary validation service (VirusTotal) is not configured in .env file.")
        return 'API Config Error', 'N/A'

    headers = {'x-apikey': THREE_KEY}
    try:
        response = session.post(THIRD_URL, headers=headers, data={'url': url},
                                timeout=10)
        response.raise_for_status()
        analysis_id = response.json()['data']['id']
        time.sleep(15)  # Allow time for the report to generate

        report_url = f'https://www.virustotal.com/api/v3/analyses/{analysis_id}'
        response = session.get(report_url, headers=headers, timeout=10)
        response.raise_for_status()

        stats = response.json()['data']['attributes']['stats']
        malicious_vendors = stats.get('malicious', 0)
        total_vendors = sum(stats.values())

        status = 'Unsafe' if malicious_vendors > 0 else 'Safe'
        ratio = f"{malicious_vendors}/{total_vendors}"
        return status, ratio


    except requests.exceptions.HTTPError as e:

        if e.response.status_code == 400:
            print(
                f"ERROR: Tertiary check failed for {url} with a 400 Bad Request. The URL may not be publicly accessible.")
            return 'Invalid URL', 'N/A'

        else:
            print(f"ERROR: Tertiary check failed for {url} with an HTTP error: {e}")
            return 'API Error', 'N/A'
    except requests.exceptions.RequestException as e:
        print(f"ERROR: Tertiary check failed for {url} with a network error: {e}")
        return 'API Error', 'N/A'

    except KeyError:
        print(f"ERROR: Could not parse VirusTotal response for {url}")
        return 'API Error', 'Parsing Failed'

"""
    Re-validates a cached link to ensure the stored result is still accurate.
    Triggers a deep scan if results are contradictory.    """
def revalidate_existing_link(existing_link, original_link, processed_link):

    with requests.Session() as session:
        cached_model_status = "Safe" if existing_link.status == "Good" else "Unsafe"

        # Always run the secondary check for re-validation
        secondary_label = run_secondary_validation(session, processed_link)

        final_status = ""
        reason = ""
        tertiary_ratio = "Not Re-checked"

        if secondary_label == 'Unsafe':
            # Rule 1: Primary service finds a threat. This is authoritative.
            final_status = 'Unsafe'
            reason = 'Re-validation confirmed the link is now flagged as malicious by a primary threat intelligence service.'

        elif cached_model_status == 'Unsafe' and secondary_label == 'Safe':
            # Rule 2: Conflict (was Bad, now seems Safe). Trigger deep scan to be sure.
            tertiary_status, tertiary_ratio = run_tertiary_validation(session, processed_link)

            if tertiary_status in ['Invalid URL', 'API Error', 'API Config Error', 'Parsing Failed']:
                final_status = 'Safe'
                reason = f'Link previously considered unsafe, but now passes primary checks. Deep analysis failed ({tertiary_status}).'

            elif tertiary_status == 'Unsafe':
                final_status = 'Unsafe'
                reason = f'Deep analysis confirmed the link remains a threat, despite passing primary checks. (Ratio: {tertiary_ratio})'
            else:
                # If deep scan also says safe, we can update the status.
                final_status = 'Safe'
                reason = f'Link previously considered unsafe, but now passes all primary and deep analysis checks. (Ratio: {tertiary_ratio})'

        elif secondary_label in ['API Error', 'API Config Error']:
            # Rule 3 (Edge Case): Re-validation failed. Trust the cache.
            final_status = cached_model_status
            reason = f'Could not re-validate link due to an API error. Displaying cached result: {final_status}.'

        else:  # Both cached model and secondary check agree it's 'Safe'
            final_status = 'Safe'
            reason = 'Result re-validated as safe by primary threat intelligence service.'

    # Update the database with the new, re-validated status if it has changed
    db_status = 'Good' if final_status == 'Safe' else 'Bad'
    if existing_link.status != db_status:
        existing_link.status = db_status
        existing_link.save()
        print(f"Updated cached status for {processed_link} to {db_status}")

    context = {
        'result': final_status,
        'display_url': original_link,
        'proceed_url': processed_link,
        'reason': reason,
        'tertiary_ratio': tertiary_ratio
    }
    return context

def perform_full_analysis(processed_link, original_link):
    """
    Orchestrates the multi-layered validation process for a new URL.
    """
    with requests.Session() as session:
        # Level 1: Local Model Prediction
        model_label = "Good" if scanner.check(processed_link) == "Good" else "Bad"

        # Level 2: Primary Threat Intelligence Check
        secondary_label = run_secondary_validation(session, processed_link)

        # --- Authoritative Decision Logic with All Edge Cases ---
        final_status = ""
        reason = ""
        tertiary_ratio = "Not Checked"

        if secondary_label == 'Unsafe':
            # Rule 1: Primary service is authoritative.
            final_status = 'Unsafe'
            reason = 'Flagged as malicious by a primary threat intelligence service.'

        elif model_label == 'Bad' and secondary_label == 'Safe':
            # Rule 2: Conflict triggers a deep scan.
            tertiary_status, tertiary_ratio = run_tertiary_validation(session, processed_link)

            if tertiary_status in ['Invalid URL', 'API Error', 'API Config Error', 'Parsing Failed']:
                final_status = 'Safe'
                reason = f'Flagged by local heuristics, but primary check was safe. Deep analysis failed ({tertiary_status}).'
            elif tertiary_status == 'Unsafe':
                final_status = 'Unsafe'
                reason = f'Confirmed malicious by deep analysis. (Detection Ratio: {tertiary_ratio})'
            else:
                final_status = 'Unsafe'
                reason = f'Flagged by local heuristics; deep analysis found no widespread threat. (Ratio: {tertiary_ratio})'

        elif model_label == 'Bad' and secondary_label == 'Unsafe':
            final_status = 'Unsafe'
            tertiary_status, tertiary_ratio = run_tertiary_validation(session, processed_link)

            if tertiary_status in ['Invalid URL', 'API Error', 'API Config Error', 'Parsing Failed']:
                reason = f'Flagged as Unsafe by primary checks. Deep analysis failed ({tertiary_status}).'
            else:
                reason = f'Confirmed malicious by multiple services. (Deep Analysis Ratio: {tertiary_ratio})'

        elif secondary_label in ['API Error', 'API Config Error']:
            final_status = 'Safe' if model_label == 'Good' else 'Unsafe'
            reason = f'Classified as {final_status} by local heuristics ({secondary_label} for threat intelligence service).'

        else:

            final_status = 'Safe'
            reason = 'Considered safe by all primary checks.'

    # Save the final, verified result to the database
    db_status = 'Good' if final_status == 'Safe' else 'Bad'
    new_link = Link(link=processed_link, status=db_status)
    new_link.save()

    # Prepare the context for the template
    context = {
        'result': final_status,
        'display_url': original_link,
        'proceed_url': processed_link,
        'reason': reason,
        'tertiary_ratio': tertiary_ratio
    }
    return context