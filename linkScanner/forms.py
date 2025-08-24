from .models import Link
from django import forms

class LinkForm(forms.ModelForm):
    class Meta:
        model = Link
        fields = ['link','status']